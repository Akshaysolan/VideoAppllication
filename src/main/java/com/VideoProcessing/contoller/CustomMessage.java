package com.VideoProcessing.contoller;



import com.VideoProcessing.entities.Video;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomMessage {

	private String message;
	private boolean success = false;
}

