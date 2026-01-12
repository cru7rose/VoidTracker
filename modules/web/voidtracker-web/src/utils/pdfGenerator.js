import jsPDF from 'jspdf';
import 'jspdf-autotable';
import { useConfigStore } from '@/stores/configStore';

export const generateManifest = (route) => {
    const configStore = useConfigStore();
    const doc = new jsPDF();
    const company = configStore.config.billing;

    // Header
    doc.setFontSize(22);
    doc.text('DELIVERY MANIFEST', 105, 20, { align: 'center' });

    doc.setFontSize(10);
    doc.text(configStore.config.general.systemName, 14, 30);
    doc.text(company.companyAddress || '', 14, 35);

    doc.text(`Route: ${route.name}`, 140, 30);
    doc.text(`Date: ${new Date().toLocaleDateString()}`, 140, 35);
    doc.text(`Driver: ${route.driver || 'Unassigned'}`, 140, 40);
    doc.text(`Vehicle: ${route.vehicle || 'N/A'}`, 140, 45);

    // Table
    const tableColumn = ["#", "Stop Address", "ETA", "Order ID", "Weight (kg)", "Signature"];
    const tableRows = [];

    route.legs.forEach((leg, index) => {
        const row = [
            index + 1,
            leg.address,
            leg.eta,
            leg.orderId || 'N/A',
            leg.weight || '0',
            '' // Empty for signature
        ];
        tableRows.push(row);
    });

    doc.autoTable({
        head: [tableColumn],
        body: tableRows,
        startY: 55,
        theme: 'grid',
        styles: { fontSize: 8, cellPadding: 3 },
        columnStyles: {
            5: { cellWidth: 40 } // Wider column for signature
        }
    });

    // Footer - Signatures
    const finalY = doc.lastAutoTable.finalY + 20;

    // Client Signature Box
    doc.setLineWidth(0.5);
    doc.rect(14, finalY, 80, 40);
    doc.text("Client Signature & Date", 16, finalY + 5);
    doc.text("_______________________", 16, finalY + 35);

    // Company Stamp Box
    doc.rect(110, finalY, 80, 40);
    doc.text("Company Stamp", 112, finalY + 5);

    // Save
    doc.save(`Manifest_${route.name}_${new Date().toISOString().slice(0, 10)}.pdf`);
};
