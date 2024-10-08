package com.VideoProcessing.services;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.VideoProcessing.entities.Video;

public interface VideoService {

	Video save(Video video, MultipartFile file);
	
	Video get(String videoId);
	
	Video getByTitle(String title);
	
	List<Video> getAll();
	
	//vido processing
	String processVideo(String videoId);
	
}
