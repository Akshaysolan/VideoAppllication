package com.VideoProcessing.contoller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
//import org.apache.tomcat.util.file.ConfigurationSource.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
//import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import com.VideoProcessing.AppConstant;
import com.VideoProcessing.entities.Video;
import com.VideoProcessing.services.VideoService;
import com.VideoProcessing.services.VideoServiceImpl;

//import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/v1/videos")
@CrossOrigin("*")
public class VideoController {
	
	private final VideoService videoService;
	
	

	// Constructor-based injection
	//@Autowired
	public VideoController(VideoService videoService) {
		this.videoService = videoService;
	}

	@PostMapping
	public ResponseEntity<?> create(
			@RequestParam("file") MultipartFile file,
			@RequestParam("title") String title,
			@RequestParam("description") String description
	){
		// Create and populate video entity
		Video video = new Video();
		video.setTitle(title);
		video.setDescription(description);
		video.setVideoId(UUID.randomUUID().toString());

		// Save video and file using VideoService
		Video savedVideo = videoService.save(video, file);
		
		if(savedVideo != null) {
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(video);
		}else {
			return ResponseEntity
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(CustomMessage.builder()
					.message("video not uploaded")
					.success(false)
					.build());
					
		}
		
	}
	
	//get all videos
	
	@GetMapping
	public List<Video>getAll(){
		return videoService.getAll();
	}
	
	//stream video..
	
	@GetMapping("/stream/{videoId}")
	public ResponseEntity<Resource> stream(
	        @PathVariable String videoId) {

	    Video video = videoService.get(videoId);
	    String contentType = video.getContentType();
	    String filePath = video.getFilePath();

	  
	    Resource resource = new FileSystemResource(filePath);


	    // Set a default content type if none is provided
	    if (contentType == null) {
	        contentType = "application/octet-stream";
	    }

	    return ResponseEntity.ok()
	            .contentType(MediaType.parseMediaType(contentType))
	            .body(resource);
	}
	
	//stream videos in chunks
    @SuppressWarnings("resource")
    @GetMapping("/stream/range/{videoId}")
    public ResponseEntity<Resource> streamVideoRange(
            @PathVariable String videoId,
            @RequestHeader(value = "Range", required = false) String range) {

        Video video = videoService.get(videoId);
        Path path = Paths.get(video.getFilePath());
        Resource resource = new FileSystemResource(path);
        String contentType = video.getContentType();

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        long fileLength = path.toFile().length();

        if (range == null) {
            // Serve full video if no range header is provided
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        }

        // Calculating start and end range
        long rangeStart = 0;
        long rangeEnd = fileLength - 1;

        try {
            String[] ranges = range.replace("bytes=", "").split("-");

            // Parse rangeStart and rangeEnd safely
            if (!ranges[0].isEmpty()) {
                rangeStart = Long.parseLong(ranges[0]);
            }

            if (ranges.length > 1 && !ranges[1].isEmpty()) {
                rangeEnd = Long.parseLong(ranges[1]);
            } else {
                rangeEnd = rangeStart + AppConstant.CHUNK_SIZE - 1;
            }

            // Ensure the rangeEnd is within the file length
            if (rangeEnd >= fileLength) {
                rangeEnd = fileLength - 1;
            }

        } catch (NumberFormatException ex) {
            // Invalid range, return bad request
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        System.out.println("Start Range: " + rangeStart);
        System.out.println("End Range: " + rangeEnd);

        try (InputStream inputStream = Files.newInputStream(path)) {
            // Skip to the start of the range
            inputStream.skip(rangeStart);

            long contentLength = rangeEnd - rangeStart + 1;
            byte[] data = new byte[(int) contentLength];
            int read = inputStream.read(data, 0, data.length);

            System.out.println("Bytes Read: " + read);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            headers.add("X-Content-Type-Options", "nosniff");
            headers.setContentLength(contentLength);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new ByteArrayResource(data));

        } catch (IOException ex) {
            // Handle file read error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Value("${file.video.hsl}")
    private String HLS_DIR;
    @GetMapping("/{videoId}/master.m3u8")
    public ResponseEntity<Resource> serverMasterFile(
    		@PathVariable String videoId
    	){
    	
    	Path path = Paths.get(HLS_DIR, videoId, "master.m3u8");
    	
    	System.out.println(path);
    	
    	if(!Files.exists(path)) {
    		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    	}
    	
    	Resource resource = new FileSystemResource(path);
    	
    	return ResponseEntity
    			.ok()
                .header(HttpHeaders.CONTENT_TYPE, "applicatioon/vnd.apple.mpegurl")
                .body(resource);

    }
    
    //Serve the segments
 // Serve the .ts video segments
    @GetMapping("/{videoId}/{segment}.ts")
    public ResponseEntity<Resource> serveSegments(
        @PathVariable String videoId,
        @PathVariable String segment
    ) {
        // Build the path to the requested segment
        Path path = Paths.get(HLS_DIR, videoId, segment + ".ts");
        System.out.println("Serving segment from Path: " + path.toString());

        // Check if the requested segment exists
        if (!Files.exists(path)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Return 404 if not found
        }

        // Create a resource from the file
        Resource resource = new FileSystemResource(path);

        try {
            // Return the .ts segment with proper headers
            return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, "video/mp2t") // Set content type for .ts
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(Files.size(path))) // Set content length
                .body(resource);
        } catch (IOException e) {
            e.printStackTrace(); // Handle any exceptions, like IO issues
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Return 500 on error
        }
    }


}










