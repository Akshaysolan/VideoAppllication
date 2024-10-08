import React, { useEffect, useRef } from 'react';
import videojs from 'video.js';
import Hls from 'hls.js';
import "video.js/dist/video-js.css";

function VideoPlayer({ src }) {
    const videoRef = useRef(null);
    const playerRef = useRef(null);

    useEffect(() => {
        // Ensure that the video element is rendered in the DOM
        if (!videoRef.current) return;

        // Wrap Video.js initialization in a timeout to allow the DOM to render
        const initializePlayer = () => {
            // Initialize Video.js if player is not already created
            if (!playerRef.current) {
                playerRef.current = videojs(videoRef.current, {
                    controls: true,
                    autoplay: true,
                    muted: true,
                    preload: "auto",
                });
            }

            // HLS support
            if (Hls.isSupported()) {
                const hls = new Hls();
                hls.loadSource(src);
                hls.attachMedia(videoRef.current);
                hls.on(Hls.Events.MANIFEST_PARSED, () => {
                    videoRef.current.play();
                });
            } 
            // Fallback for native HLS support (Safari)
            else if (videoRef.current.canPlayType("application/vnd.apple.mpegurl")) {
                videoRef.current.src = src;
                videoRef.current.addEventListener("canplay", () => {
                    videoRef.current.play();
                });
            } else {
                console.error("Video format not supported");
            }
        };

        // Initialize player after a brief delay
        const timeoutId = setTimeout(initializePlayer, 100);

        // Cleanup on unmount
        return () => {
            if (playerRef.current) {
                playerRef.current.dispose();
                playerRef.current = null;
            }
            clearTimeout(timeoutId);
        };
    }, [src]);

    return (
        <div>
            <div data-vjs-player>
                <video
                    ref={videoRef}
                    style={{
                        width: "100%",
                        height: "400px",
                    }}
                    className="video-js vjs-control-bar"
                ></video>
            </div>
        </div>
    );
}

export default VideoPlayer;

