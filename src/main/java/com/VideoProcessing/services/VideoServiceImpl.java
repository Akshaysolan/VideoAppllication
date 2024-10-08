package com.VideoProcessing.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.VideoProcessing.entities.Video;
import com.VideoProcessing.repository.VideoRepository;

import jakarta.annotation.PostConstruct;




@Service
public class VideoServiceImpl implements VideoService{


	@Value("${files.video}")
	String DIR;
	
	@Value("${file.video.hsl}")
	String HSL_DIR;
	
	
	private VideoRepository videoRepository;
	
	
	public VideoServiceImpl(VideoRepository videoRepository) {
		
		this.videoRepository = videoRepository;
	}

	@PostConstruct
	public void init() {
		
		File file = new File(DIR);
		
		
		
		if(!file.exists()) {
			file.mkdir();
			System.out.println("Folder created");
		}else {
			System.out.println("Folder already created");
		}
		
//		try {
//			Files.createDirectories(Paths.get(HSL_DIR));
//		} catch (IOException e) {
//			
//			e.printStackTrace();
//		}
        File file1 = new File(HSL_DIR);
		
		if(!file1.exists()) {
			file1.mkdir();
			System.out.println("Folder created");
		}else {
			System.out.println("Folder already created");
		}
			
	}
	
	@Override
	public Video save(Video video, MultipartFile file) {
		
		//Original file name
		
		
	    try {
	    	
	    	//file path
	    	String filename = file.getOriginalFilename();
			
			String contentType = file.getContentType();
			InputStream inputStream = file.getInputStream();
			
			//folder path : create
			
			String cleanFilename = StringUtils.cleanPath(filename);
			String cleanFolder = StringUtils.cleanPath(DIR);
			
			//folder path with filename
			Path path = Paths.get(cleanFolder, cleanFilename);
			
			System.out.println(contentType);
			System.out.println(path);
			
			
			
			//copy file 
			Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
			//video meta data
			video.setContentType(contentType);
			video.setFilePath(path.toString());
						
			videoRepository.save(video);
			
			//process video
			processVideo(video.getVideoId());
			
			//Delete actual video file and database entry if exception
			
			
			//meta data save

			return video;
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}		
	}

	@Override
	public Video get(String videoId) {
	
		Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("video not found"));
         
		return video;

	}

	@Override
	public Video getByTitle(String title) {
		
		return null;
	}

	@Override
	public List<Video> getAll() {
		
		return videoRepository.findAll();
	}

	
	@Override
	public String processVideo(String videoId) {
	    try {
	        // Fetch the video details using the videoId
	        Video video = this.get(videoId);

	        // Get the file path from the video object
	        String filePath = video.getFilePath();

	        // Path where to store the HLS data
	        Path videoPath = Paths.get(filePath);
	        Path outputPath = Paths.get(HSL_DIR, videoId);

	        // Create output directories if not exist
	        Files.createDirectories(outputPath);

	        // FFmpeg command to convert video to HLS format
	        String ffmpeg = String.format(
	            "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 " +
	            "-hls_segment_filename \"%s/segment_%%03d.ts\" \"%s/master.m3u8\" ",
	            videoPath, outputPath, outputPath
	        );

	        // Determine the operating system
	        String os = System.getProperty("os.name").toLowerCase();
	        ProcessBuilder processBuilder;

	        // Use "cmd.exe" for Windows, and "/bin/bash" for Unix-based systems
	        if (os.contains("win")) {
	            // Windows
	            processBuilder = new ProcessBuilder("cmd.exe", "/c", ffmpeg);
	        } else {
	            // Unix-based (Linux/Mac)
	            processBuilder = new ProcessBuilder("/bin/bash", "-c", ffmpeg);
	        }

	        processBuilder.inheritIO(); // Ensures that the output from FFmpeg is shown in the console

	        // Start the process
	        Process process = processBuilder.start();

	        // Wait for the process to complete
	        int exitCode = process.waitFor();

	        // Check if the process exited successfully
	        if (exitCode != 0) {
	            throw new RuntimeException("Video processing failed!!");
	        }

	        // Return the videoId upon successful processing
	        return videoId;

	    } catch (IOException e) {
	        e.printStackTrace();
	        throw new RuntimeException("IO exception during video processing", e);
	    } catch (InterruptedException e) {
	        throw new RuntimeException("Video processing interrupted", e);
	    }
	}

	
}
