import { useState } from "react";
import "./App.css";

function App() {
  const [formData, setFormData] = useState({
    date: new Date().toISOString().split("T")[0],
    version: "",
    comment: "",
  });

  const [response, setResponse] = useState("");

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const publishAnnotation = async () => {
    try {
      const res = await fetch("http://localhost:8080/api/annotations", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(formData),
      });

      if (res.ok) {
        setResponse("Annotation published successfully.");
      } else {
        setResponse("Failed to publish annotation.");
      }
    } catch (err) {
      setResponse("Backend connection failed.");
    }
  };

  return (
      <div className="container">
        <h1>Grafana Annotation Publisher</h1>

        <p className="description">
          This publisher pushes annotation events into Grafana Loki so they can
          later be visualized, searched, and correlated inside Grafana dashboards.
          It can be used to publish deployment markers, release events, outage
          annotations, operational comments, and version tracking metadata.
        </p>

        <div className="form">
          <label>Date</label>
          <input
              type="date"
              name="date"
              value={formData.date}
              onChange={handleChange}
          />

          <label>Application Version</label>
          <input
              type="text"
              name="version"
              placeholder="v1.0.0"
              value={formData.version}
              onChange={handleChange}
          />

          <label>Comment</label>
          <textarea
              name="comment"
              placeholder="Deployment completed successfully..."
              value={formData.comment}
              onChange={handleChange}
          />

          <button onClick={publishAnnotation}>
            Publish Annotation
          </button>

          {response && <p className="response">{response}</p>}
        </div>
      </div>
  );
}

export default App;