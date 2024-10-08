package com.VideoProcessing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.VideoProcessing.services.VideoService;

@SpringBootTest
class VideoApplicationTests {

	@Autowired
	VideoService videoService;
	@Test
	void contextLoads() {
		
		videoService.processVideo("c29a9c81-b90e-4001-a5b7-0c98291bc2f5");
	}

}
