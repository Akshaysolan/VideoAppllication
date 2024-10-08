import { useState, useEffect } from 'react';
import './App.css';
import VideoUploads from './components/VideoUploads';
import { ToastContainer } from 'react-toastify';
import VideoPlayer from './components/VideoPlayer';
import { Button, TextInput } from 'flowbite-react';

function App() {
  const [videoId, setVideoId] = useState('c29a9c81-b90e-4001-a5b7-0c98291bc2f5');
  const [fieldValue, setFieldValue] = useState('');

  useEffect(() => {
    console.log("Video ID changed to:", videoId);
  }, [videoId]);

  function handlePlayClick() {
    if (fieldValue.trim()) {
      setVideoId(fieldValue);
    }
  }

  return (
    <>
      <ToastContainer />
      <div className='flex flex-col items-center space-y-9 justify-center py-9 px-4 sm:px-6 lg:px-8'>
        <h1 className='text-4xl sm:text-5xl font-extrabold text-gray-700 dark:text-gray-100'>Video Streaming App</h1>
        <div className='flex flex-col md:flex-row w-full justify-around'>
          <div className='w-full md:w-1/2 lg:w-1/3'>
            <h1 className='text-white mb-2 text-center'>Playing Video</h1>
            <div className='flex flex-col space-y-2 md:space-y-0 md:flex-row md:space-x-2'>
              <TextInput
                onChange={(event) => setFieldValue(event.target.value)}
                placeholder="Enter video ID"
                name="video_id_field"
                value={fieldValue}
                className="w-full" // Make input take full width
              />
              <Button onClick={handlePlayClick} className="w-full md:w-auto">Play</Button>
            </div>
            <div className='mt-4 w-full'>
              {videoId && (
                <VideoPlayer src={`http://localhost:8081/api/v1/videos/${videoId}/master.m3u8`} />
              )}
            </div>
          </div>
          <VideoUploads />
        </div>
      </div>
    </>
  );
}

export default App;
