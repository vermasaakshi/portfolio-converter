import React, { useState } from 'react';
import { Upload, FileText, Globe, CheckCircle, AlertCircle, Loader } from 'lucide-react';

const ResumeParser = () => {
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [parsing, setParsing] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [extractedData, setExtractedData] = useState(null);
  const [step, setStep] = useState(1);
  const [error, setError] = useState('');

  const handleFileSelect = (event) => {
    const selectedFile = event.target.files[0];
    if (selectedFile) {
      const allowedTypes = [
        'application/pdf', 
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 
        'application/msword'
      ];
      if (allowedTypes.includes(selectedFile.type)) {
        setFile(selectedFile);
        setError('');
      } else {
        setError('Please select a PDF, DOCX, or DOC file');
        setFile(null);
      }
    }
  };

  const handleUpload = async () => {
    if (!file) return;
    setUploading(true);
    setError('');

    try {
      const formData = new FormData();
      formData.append('file', file);

      const response = await fetch('http://localhost:8080/upload', {
        method: 'POST',
        body: formData,
      });

      if (!response.ok) {
        throw new Error('Upload failed');
      }
      setStep(2);
      setTimeout(() => handleParse(), 1000);
    } catch (err) {
      setError('Failed to upload file. Please try again.');
    } finally {
      setUploading(false);
    }
  };

  const handleParse = async () => {
    setParsing(true);
    setError('');

    try {
      const response = await fetch('http://localhost:8080/parse', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ fileName: file.name }),
      });

      if (!response.ok) {
        throw new Error('Parsing failed');
      }

      const data = await response.json();
      setExtractedData(data);
      setStep(3);
    } catch (err) {
      setError('Failed to parse resume. Please try again.');
    } finally {
      setParsing(false);
    }
  };

  const handleGenerateWebsite = async () => {
    setGenerating(true);
    setError('');

    try {
      const response = await fetch('http://localhost:8080/generate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(extractedData),
      });

      if (!response.ok) {
        throw new Error('Website generation failed');
      }

      const result = await response.json();
      window.open(result.websiteUrl || 'http://localhost:8080/portfolio', '_blank');
    } catch (err) {
      setError('Failed to generate website. Please try again.');
    } finally {
      setGenerating(false);
    }
  };

  const resetProcess = () => {
    setFile(null);
    setExtractedData(null);
    setStep(1);
    setError('');
    setUploading(false);
    setParsing(false);
    setGenerating(false);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 py-8">
      <div className="container mx-auto px-4">
        <div className="max-w-4xl mx-auto">
          {/* Header */}
          <div className="text-center mb-8">
            <h1 className="text-4xl font-bold text-gray-800 mb-4">
              Resume Parser & Website Generator
            </h1>
            <p className="text-lg text-gray-600">
              Upload your resume and get a beautiful portfolio website instantly
            </p>
          </div>

          {/* Progress Steps */}
          <div className="flex justify-center mb-8">
            <div className="flex items-center space-x-4">
              <div className={`flex items-center space-x-2 px-4 py-2 rounded-full ${step >= 1 ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-500'}`}>
                <Upload size={20}/>
                <span>Upload</span>
              </div>
              <div className={`w-8 h-1 ${step >= 2 ? 'bg-blue-500' : 'bg-gray-300'}`}></div>
              <div className={`flex items-center space-x-2 px-4 py-2 rounded-full ${step >= 2 ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-500'}`}>
                <FileText size={20}/>
                <span>Parse</span>
              </div>
              <div className={`w-8 h-1 ${step >= 3 ? 'bg-blue-500' : 'bg-gray-300'}`}></div>
              <div className={`flex items-center space-x-2 px-4 py-2 rounded-full ${step >= 3 ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-500'}`}>
                <Globe size={20}/>
                <span>Generate</span>
              </div>
            </div>
          </div>

          {/* Error Message */}
          {error && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-6 flex items-center">
              <AlertCircle size={20} className="mr-2" />
              {error}
            </div>
          )}

          {/* Step 1: File Upload */}
          {step === 1 && (
            <div className="bg-white rounded-lg shadow-lg p-8 mb-6">
              <h2 className="text-2xl font-semibold mb-6 text-center">Upload Your Resume</h2>
              <div className="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center hover:border-blue-400 transition-colors">
                <input
                  type="file"
                  accept=".pdf,.docx,.doc"
                  onChange={handleFileSelect}
                  className="hidden"
                  id="file-upload"
                />
                <label htmlFor="file-upload" className="cursor-pointer">
                  <Upload size={48} className="mx-auto mb-4 text-gray-400" />
                  <p className="text-lg text-gray-600 mb-2">
                    Click to upload or drag and drop
                  </p>
                  <p className="text-sm text-gray-500">
                    PDF, DOCX, or DOC files only
                  </p>
                </label>
              </div>

              {file && (
                <div className="mt-6 p-4 bg-gray-50 rounded-lg">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-3">
                      <FileText size={24} className="text-blue-500"/>
                      <div>
                        <p className="font-medium">{file.name}</p>
                        <p className="text-sm text-gray-500">
                          {(file.size / 1024 / 1024).toFixed(2)} MB
                        </p>
                      </div>
                    </div>
                    <button
                      onClick={handleUpload}
                      disabled={uploading}
                      className="bg-blue-500 hover:bg-blue-600 text-white px-6 py-2 rounded-lg disabled:opacity-50 flex items-center space-x-2"
                    >
                      {uploading ? (
                        <>
                          <Loader size={16} className="animate-spin" />
                          <span>Uploading...</span>
                        </>
                      ) : (
                        <>
                          <Upload size={16} />
                          <span>Upload</span>
                        </>
                      )}
                    </button>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Step 2: Parsing */}
          {step === 2 && (
            <div className="bg-white rounded-lg shadow-lg p-8 mb-6">
              <h2 className="text-2xl font-semibold mb-6 text-center">Parsing Your Resume</h2>
              <div className="text-center">
                <Loader size={48} className="animate-spin mx-auto mb-4 text-blue-500" />
                <p className="text-lg text-gray-600">
                  Extracting information from your resume...
                </p>
              </div>
            </div>
          )}

          {/* Step 3: Display Extracted Data */}
          {step === 3 && extractedData && (
            <div className="bg-white rounded-lg shadow-lg p-8 mb-6">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-2xl font-semibold">Extracted Information</h2>
                <CheckCircle size={24} className="text-green-500" />
              </div>

              <div className="grid md:grid-cols-2 gap-6">
                {/* Personal Information */}
                <div className="bg-gray-50 p-6 rounded-lg">
                  <h3 className="text-lg font-semibold mb-4">Personal Information</h3>
                  <div className="space-y-2">
                    <p><strong>Name:</strong> {extractedData.personalInfo.name}</p>
                    <p><strong>Email:</strong> {extractedData.personalInfo.email}</p>
                    <p><strong>Phone:</strong> {extractedData.personalInfo.phone}</p>
                    <p><strong>Address:</strong> {extractedData.personalInfo.address}</p>
                  </div>
                </div>

                {/* Skills */}
                <div className="bg-gray-50 p-6 rounded-lg">
                  <h3 className="text-lg font-semibold mb-4">Skills</h3>
                  <div className="flex flex-wrap gap-2">
                    {extractedData.skills.map((skill, index) => (
                      <span key={index} className="bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-sm">
                        {skill}
                      </span>
                    ))}
                  </div>
                </div>

                {/* Education */}
                <div className="bg-gray-50 p-6 rounded-lg">
                  <h3 className="text-lg font-semibold mb-4">Education</h3>
                  {extractedData.education.map((edu, index) => (
                    <div key={index} className="mb-4">
                      <h4 className="font-medium">{edu.degree}</h4>
                      <p className="text-sm text-gray-600">{edu.institution}</p>
                      <p className="text-sm text-gray-600">{edu.year} • GPA: {edu.gpa}</p>
                    </div>
                  ))}
                </div>

                {/* Experience */}
                <div className="bg-gray-50 p-6 rounded-lg">
                  <h3 className="text-lg font-semibold mb-4">Experience</h3>
                  {extractedData.experience.map((exp, index) => (
                    <div key={index} className="mb-4">
                      <h4 className="font-medium">{exp.position}</h4>
                      <p className="text-sm text-gray-600">{exp.company} • {exp.duration}</p>
                      <p className="text-sm text-gray-700 mt-1">{exp.description}</p>
                    </div>
                  ))}
                </div>
              </div>

              <div className="mt-8 text-center">
                <button
                  onClick={handleGenerateWebsite}
                  disabled={generating}
                  className="bg-green-500 hover:bg-green-600 text-white px-8 py-3 rounded-lg text-lg font-semibold disabled:opacity-50 flex items-center space-x-2 mx-auto"
                >
                  {generating ? (
                    <>
                      <Loader size={20} className="animate-spin" />
                      <span>Generating Website...</span>
                    </>
                  ) : (
                    <>
                      <Globe size={20} />
                      <span>Generate Portfolio Website</span>
                    </>
                  )}
                </button>
              </div>
            </div>
          )}

          {/* Reset Button */}
          {step > 1 && (
            <div className="text-center">
              <button
                onClick={resetProcess}
                className="bg-gray-500 hover:bg-gray-600 text-white px-6 py-2 rounded-lg"
              >
                Start Over
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ResumeParser;