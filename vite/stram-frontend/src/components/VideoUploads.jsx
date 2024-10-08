import React, { useState } from 'react';
import { Button, Card, TextInput, Textarea, Progress, Alert } from 'flowbite-react';
import axios from 'axios';
import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

function VideoUploads() {
  const [meta, setMeta] = useState({
    title: "",
    description: ""
  });
  const [selectedFile, setSelectedFile] = useState(null);
  const [progress, setProgress] = useState(0); // Fix typo (progress)
  const [uploading, setUploading] = useState(false);
  const [message, setMessage] = useState("");

  function handleFileChange(event) {
    setSelectedFile(event.target.files[0]);
  }

  function handleMetaChange(event) {
    const { name, value } = event.target;
    setMeta((prevMeta) => ({
      ...prevMeta,
      [name]: value
    }));
  }

  function resetForm() {
    setMeta({
      title: "",
      description: "",

    });
    setSelectedFile(null);
    setUploading(false);
    //setMessage("");
  }

  async function saveVideoToServer(video, videoMeta) {
    setUploading(true);
    const formData = new FormData();
    formData.append("title", videoMeta.title);
    formData.append("description", videoMeta.description);
    formData.append("file", video); 

    try {
      const response = await axios.post(
        `http://localhost:8081/api/v1/videos`,
        formData,
        {
          headers: {
            "Content-Type": "multipart/form-data"
          },

          onUploadProgress: (progressEvent) => {

            const percentage = Math.round(
              (progressEvent.loaded * 100) / progressEvent.total
            );

            setProgress(percentage);
            console.log(`Upload progress: ${percentage}%`);
          }
        }
      );

      setMessage("File uploaded successfully!" + response.data.videoId);
      console.log(response);
      setProgress(0);
      toast.success("File uploaded successfully!");
      resetForm();

    } catch (error) {

      setMessage("File upload failed!");
      console.error(error);
      toast.error("File upload failed!");
    } finally {
      setUploading(false);
    }
  }

  function handleForm(event) {
    event.preventDefault(); // Prevent form submission from refreshing the page

    if (!selectedFile) {
      alert("Please select a file!");
      return;
    }

    saveVideoToServer(selectedFile, meta); // Pass the `selectedFile` and `meta` correctly
  }

  return (
    <div className="text-white">
      <Card className="bg-slate-400">
        <form onSubmit={handleForm} className="p-4">
          {/* Title Input */}
          <div className="mb-2">
            <label
              htmlFor="video_title"
              className="block mb-2 text-sm font-medium text-gray-900 dark:text-white"
            >
              Video Title
            </label>
            <TextInput
              id="video_title"
              name="title"
              placeholder="Enter title"
              sizing="sm"
              value={meta.title}
              onChange={handleMetaChange}
              noValidate
            />
          </div>

          {/* Description Input */}
          <div className="max-w-md mb-2">
            <label
              htmlFor="video_description"
              className="block mb-2 text-sm font-medium text-gray-900 dark:text-white"
            >
              Video Description
            </label>
            <Textarea
              id="video_description"
              name="description"
              placeholder="Write video description..."
              rows={4}
              value={meta.description}
              onChange={handleMetaChange}
              noValidate
            />
          </div>

          {/* File Input */}
          <label
            className="block mb-2 text-sm font-medium text-gray-900 dark:text-white"
            htmlFor="file_input"
          >
            Upload File
          </label>
          <input
            onChange={handleFileChange}
            className="block w-full text-sm text-gray-900 border border-gray-300 rounded-lg cursor-pointer bg-gray-50 dark:text-gray-400 focus:outline-none dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400"
            id="file_input"
            name="file"
            type="file"
            noValidate
          />

          {/* progress*/}
          <div className='mt-2 rounded-xl'>
            {uploading && (
              <div className="mt-2 rounded-xl">
                <Progress progress={progress} size="lg" color="blue" />
                <span>{progress}% - Uploading...</span>
              </div>
            )}
          </div >
          {message && (
            <Alert
              color={"success"}
              className="flex w-full justify-between flex-row items-center gap-4 bg-green-400"
            > 
              <p className="text-sm mt-2">{message}</p>

              <button
                type="button"
                className="text-white hover:text-gray-200 font-bold text-xl"
                onClick={() => setMessage("")}
              >
                &times; 
              </button>
            </Alert>
          )}

          {/* Submit Button */}
          <Button
            type="submit"
            className="p-1 m-3 bg-green-400"
            disabled={uploading}>
            Submit
          </Button>


        </form>
      </Card>
    </div>
  );
}

export default VideoUploads;
