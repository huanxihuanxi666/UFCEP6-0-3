/**
 * Paste this complete file into Postman Mock's code editor, then click Start.
 * It keeps the BPMN-safe single-patient route and adds a presentation-only
 * route that returns two richer patient records.
 */
const http = require("http");

const PORT = process.env.PORT || 4500;
const JSON_HEADER = { "Content-Type": "application/json" };

const nextPatient = {
  patientId: "P1001",
  patientName: "John Smith",
  referralType: "Cardiology",
  referralStatus: "NEW"
};

const demonstrationRecords = {
  recordCount: 2,
  patients: [
    {
      patientId: "P1001",
      patientName: "John Smith",
      referralType: "Cardiology",
      referralStatus: "NEW",
      priority: "URGENT",
      contactNumber: "07123456789",
      requestedAppointmentDate: "2026-10-24"
    },
    {
      patientId: "P1002",
      patientName: "Aisha Khan",
      referralType: "Oncology",
      referralStatus: "PENDING_REVIEW",
      priority: "ROUTINE",
      contactNumber: "07987654321",
      requestedAppointmentDate: "2026-10-29"
    }
  ]
};

const server = http.createServer((req, res) => {
  if (req.method === "GET" && req.url === "/patients/next") {
    res.writeHead(200, JSON_HEADER);
    res.end(JSON.stringify(nextPatient));
    return;
  }

  if (req.method === "GET" && req.url === "/patients/demo-records") {
    res.writeHead(200, JSON_HEADER);
    res.end(JSON.stringify(demonstrationRecords));
    return;
  }

  res.writeHead(404, JSON_HEADER);
  res.end(JSON.stringify({ error: "Route not found" }));
});

server.listen(PORT);
