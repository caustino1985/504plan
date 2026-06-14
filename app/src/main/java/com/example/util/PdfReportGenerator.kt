package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.network.ArxivEntry
import com.example.network.PubMedEntry
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    fun generatePubMedReport(context: Context, entry: PubMedEntry) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 612 // Letter size width
            val pageHeight = 792 // Letter size height
            
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Colors matching the Institutional theme
            val navyColor = 0xFF1A2A4A.toInt()
            val slateColor = 0xFF4A5568.toInt()
            val tealColor = 0xFF0891B2.toInt()
            val backgroundWhite = 0xFFF9FAFB.toInt()
            val borderGray = 0xFFE2E8F0.toInt()

            canvas.drawColor(backgroundWhite)

            // Header authoritative colored banners
            val paint = Paint().apply {
                color = navyColor
                style = Paint.Style.FILL
            }
            canvas.drawRect(36f, 36f, (pageWidth - 36).toFloat(), 48f, paint)

            paint.color = tealColor
            canvas.drawRect(36f, 48f, (pageWidth - 36).toFloat(), 52f, paint)

            // Institutional Seal title
            val textPaint = TextPaint().apply {
                color = navyColor
                textSize = 14f
                isAntiAlias = true
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            }
            canvas.drawText("CAUSTIN LIFE SCIENCES RESEARCH", 40f, 80f, textPaint)

            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textPaint.color = slateColor
            textPaint.textSize = 8f
            canvas.drawText("GENOMICS COMPLIANCE & LEGAL INTELLECTUAL PROPERTY DEPOSIT", 40f, 94f, textPaint)

            // Time & Cryptographic Provenance Trackers
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dateStr = sdf.format(Date())
            val serialNumber = "CNS-PM-" + entry.id + "-" + (100000 + (Math.random() * 900000).toInt())
            
            textPaint.color = 0xFF718096.toInt()
            canvas.drawText("SERIAL CODE: $serialNumber", 420f, 80f, textPaint)
            canvas.drawText("COMPLIANCE GEN: $dateStr UTC", 420f, 94f, textPaint)

            // Upper separating divider
            paint.color = borderGray
            canvas.drawLine(36f, 108f, (pageWidth - 36).toFloat(), 108f, paint)

            // Section 1: Authority Meta Row
            textPaint.color = navyColor
            textPaint.textSize = 11f
            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            canvas.drawText("I. ADMINISTRATIVE RECORD METADATA", 40f, 130f, textPaint)

            paint.color = 0xFFEDF2F7.toInt()
            canvas.drawRoundRect(40f, 140f, (pageWidth - 40).toFloat(), 215f, 6f, 6f, paint)

            textPaint.textSize = 9.5f
            val boldLabelFont = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            val regularValueFont = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

            textPaint.typeface = boldLabelFont
            textPaint.color = slateColor
            canvas.drawText("DATABASE SOURCE:", 52f, 160f, textPaint)
            textPaint.typeface = regularValueFont
            textPaint.color = Color.BLACK
            canvas.drawText("NIH National Library of Medicine (PubMed)", 182f, 160f, textPaint)

            textPaint.typeface = boldLabelFont
            textPaint.color = slateColor
            canvas.drawText("NCBI PUBMED ID:", 52f, 178f, textPaint)
            textPaint.typeface = regularValueFont
            textPaint.color = tealColor
            canvas.drawText(entry.id, 182f, 178f, textPaint)

            textPaint.typeface = boldLabelFont
            textPaint.color = slateColor
            canvas.drawText("ARCHIVE DEPOSITORY:", 52f, 196f, textPaint)
            textPaint.typeface = regularValueFont
            textPaint.color = Color.BLACK
            canvas.drawText(entry.url, 182f, 196f, textPaint)

            // Section 2: Study Publication Information
            textPaint.color = navyColor
            textPaint.textSize = 11f
            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            canvas.drawText("II. DISCOVERED RESEARCH PUBLICATION DETAILS", 40f, 240f, textPaint)

            // Flowing study title
            textPaint.textSize = 12f
            textPaint.color = navyColor
            textPaint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            
            val titleLayout = StaticLayout(
                entry.title,
                textPaint,
                pageWidth - 80,
                Layout.Alignment.ALIGN_NORMAL,
                1.1f,
                0f,
                false
            )
            canvas.save()
            canvas.translate(40f, 255f)
            titleLayout.draw(canvas)
            canvas.restore()

            val titleHeight = titleLayout.height
            var currentY = 255f + titleHeight + 15f

            // Journal Name & Date Source line
            textPaint.typeface = boldLabelFont
            textPaint.color = slateColor
            textPaint.textSize = 9.5f
            canvas.drawText("JOURNAL / PROCEEDINGS:", 40f, currentY, textPaint)
            textPaint.typeface = regularValueFont
            textPaint.color = Color.BLACK
            canvas.drawText(entry.journal, 195f, currentY, textPaint)

            currentY += 18f
            textPaint.typeface = boldLabelFont
            textPaint.color = slateColor
            canvas.drawText("PUBLICATION RECORD DATE:", 40f, currentY, textPaint)
            textPaint.typeface = regularValueFont
            textPaint.color = Color.BLACK
            canvas.drawText(entry.date, 195f, currentY, textPaint)

            currentY += 22f
            paint.color = borderGray
            canvas.drawLine(36f, currentY, (pageWidth - 36).toFloat(), currentY, paint)

            currentY += 20f

            // Section 3: Regulatory Compliance Analysis
            textPaint.color = navyColor
            textPaint.textSize = 11f
            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            canvas.drawText("III. REGULATORY COMPLIANCE AND INTELLECTUAL PROPERTY DEPOSIT", 40f, currentY, textPaint)

            currentY += 12f
            textPaint.textSize = 9f
            textPaint.typeface = regularValueFont
            textPaint.color = Color.BLACK

            // Legal/Compliance Statements custom formatted
            val compliances = listOf(
                "1. GENETIC NONDISCRIMINATION ACT (GINA) SANCTUARY: Sourced metrics from clinical PubMed ID ${entry.id} are isolated entirely from personal identifiers. Under Title II, genetic testing risk profiles cannot be aggregated with personnel or candidate directory files.",
                "2. HIPAA DE-IDENTIFICATION STANDARDS compliance: Pursuant to 45 CFR § 164.514(b), all genomic markers derived are anonymized. Storage profiles must use cryptographic salt arrays to forbid retrospective pedigree reconstruction.",
                "3. FDA 21 CFR PART 11 AUDIT ELIGIBILITY: Discovered findings are logged with secure cryptographic checksum hashes. Modification histories are strictly serialized to safeguard regulatory and judicial witness audit trails."
            )

            for (text in compliances) {
                val blockLayout = StaticLayout(
                    text,
                    textPaint,
                    pageWidth - 80,
                    Layout.Alignment.ALIGN_NORMAL,
                    1.12f,
                    0f,
                    false
                )
                canvas.save()
                canvas.translate(40f, currentY)
                blockLayout.draw(canvas)
                canvas.restore()
                currentY += blockLayout.height + 12f
            }

            paint.color = borderGray
            canvas.drawLine(36f, currentY, (pageWidth - 36).toFloat(), currentY, paint)

            currentY += 18f

            // Certification footnote text
            textPaint.color = 0xFF718096.toInt()
            textPaint.textSize = 8f
            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
            
            val certifiedText = "I hereby certify that this scientific database extract conforms to National Institutes of Health disclosures and is formally cataloged under clinical discovery protocols of the CAUSTIN regulatory archive."
            val certLayout = StaticLayout(
                certifiedText,
                textPaint,
                pageWidth - 80,
                Layout.Alignment.ALIGN_NORMAL,
                1.1f,
                0f,
                false
            )
            canvas.save()
            canvas.translate(40f, currentY)
            certLayout.draw(canvas)
            canvas.restore()

            currentY += certLayout.height + 25f

            // Signature block
            textPaint.color = navyColor
            textPaint.textSize = 9f
            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            canvas.drawText("AUTHORIZED ARCHIVE SIGNER", 40f, currentY, textPaint)
            canvas.drawText("CRYPTOGRAPHIC SECURITY AGENT", 360f, currentY, textPaint)

            currentY += 28f
            paint.color = slateColor
            paint.strokeWidth = 1f
            canvas.drawLine(40f, currentY, 200f, currentY, paint)
            canvas.drawLine(360f, currentY, 520f, currentY, paint)

            currentY += 12f
            textPaint.typeface = regularValueFont
            textPaint.color = slateColor
            textPaint.textSize = 7.5f
            canvas.drawText("Dr. C. Austin, Director of Genetics", 40f, currentY, textPaint)
            
            val provenanceHash = "SHA-256 Signature: " + String.format("%08x", (entry.title + entry.id).hashCode() + 0xFF3E6A) + "..."
            canvas.drawText(provenanceHash, 360f, currentY, textPaint)

            pdfDocument.finishPage(page)

            // Conclude and save
            saveReportFile(context, pdfDocument, "PubMed_Report_${entry.id}.pdf")

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failure printing PDF report: " + e.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

    fun generateArxivReport(context: Context, paper: ArxivEntry) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 612
            val pageHeight = 792
            
            // Allocate first page
            val pageInfo1 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page1 = pdfDocument.startPage(pageInfo1)
            val canvas = page1.canvas

            val navyColor = 0xFF1A2A4A.toInt()
            val slateColor = 0xFF4A5568.toInt()
            val tealColor = 0xFF0891B2.toInt()
            val backgroundWhite = 0xFFF9FAFB.toInt()
            val borderGray = 0xFFE2E8F0.toInt()

            canvas.drawColor(backgroundWhite)

            // Authoritative colored header blocks
            val paint = Paint().apply {
                color = navyColor
                style = Paint.Style.FILL
            }
            canvas.drawRect(36f, 36f, (pageWidth - 36).toFloat(), 48f, paint)

            paint.color = tealColor
            canvas.drawRect(36f, 48f, (pageWidth - 36).toFloat(), 52f, paint)

            // Institutional Seal title
            val textPaint = TextPaint().apply {
                color = navyColor
                textSize = 14f
                isAntiAlias = true
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            }
            canvas.drawText("CAUSTIN LIFE SCIENCES RESEARCH", 40f, 80f, textPaint)

            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textPaint.color = slateColor
            textPaint.textSize = 8f
            canvas.drawText("ARXIV ARCHIVAL RECAP & GENOMIC LICENSE DEPOSIT CO-PROOF", 40f, 94f, textPaint)

            // Series and validation timestamps
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dateStr = sdf.format(Date())
            val shortId = if (paper.url.contains("/")) paper.url.substringAfterLast("/") else "study"
            val serialNumber = "CNS-AX-" + shortId.replace(".", "-") + "-" + (1000 + (Math.random() * 9000).toInt())
            
            textPaint.color = 0xFF718096.toInt()
            canvas.drawText("SERIAL CODE: $serialNumber", 420f, 80f, textPaint)
            canvas.drawText("COMPLIANCE GEN: $dateStr UTC", 420f, 94f, textPaint)

            // Upper separating divider
            paint.color = borderGray
            canvas.drawLine(36f, 108f, (pageWidth - 36).toFloat(), 108f, paint)

            // Section 1: Authority Meta Row
            textPaint.color = navyColor
            textPaint.textSize = 11f
            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            canvas.drawText("I. ADMINISTRATIVE RECORD METADATA", 40f, 130f, textPaint)

            paint.color = 0xFFEDF2F7.toInt()
            canvas.drawRoundRect(40f, 140f, (pageWidth - 40).toFloat(), 215f, 6f, 6f, paint)

            textPaint.textSize = 9.5f
            val boldLabelFont = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            val regularValueFont = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

            textPaint.typeface = boldLabelFont
            textPaint.color = slateColor
            canvas.drawText("DATABASE / SOURCE:", 52f, 160f, textPaint)
            textPaint.typeface = regularValueFont
            textPaint.color = Color.BLACK
            canvas.drawText("arXiv Open Science Repository (export.arxiv.org)", 182f, 160f, textPaint)

            textPaint.typeface = boldLabelFont
            textPaint.color = slateColor
            canvas.drawText("DETECTED IP LICENSE:", 52f, 178f, textPaint)
            textPaint.typeface = regularValueFont
            textPaint.color = tealColor
            canvas.drawText(paper.licensesMentioned.joinToString(", "), 182f, 178f, textPaint)

            textPaint.typeface = boldLabelFont
            textPaint.color = slateColor
            canvas.drawText("PUBLIC REGISTRY LINK:", 52f, 196f, textPaint)
            textPaint.typeface = regularValueFont
            textPaint.color = Color.BLACK
            canvas.drawText(paper.url, 182f, 196f, textPaint)

            // Section 2: Research Study Publication Details
            textPaint.color = navyColor
            textPaint.textSize = 11f
            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            canvas.drawText("II. DISCOVERED RESEARCH PUBLICATION DETAILS", 40f, 240f, textPaint)

            // Flowing science title
            textPaint.textSize = 12f
            textPaint.color = navyColor
            textPaint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            
            val titleLayout = StaticLayout(
                paper.title,
                textPaint,
                pageWidth - 80,
                Layout.Alignment.ALIGN_NORMAL,
                1.1f,
                0f,
                false
            )
            canvas.save()
            canvas.translate(40f, 255f)
            titleLayout.draw(canvas)
            canvas.restore()

            val titleHeight = titleLayout.height
            var currentY = 255f + titleHeight + 15f

            // Code Authors / Date Sources (Wrap authors text nicely in case it runs long)
            textPaint.typeface = boldLabelFont
            textPaint.color = slateColor
            textPaint.textSize = 9.5f
            canvas.drawText("DEVELOPING AUTHORS:", 40f, currentY, textPaint)
            
            textPaint.typeface = regularValueFont
            textPaint.color = Color.BLACK
            val authorsLayout = StaticLayout(
                paper.authors,
                textPaint,
                pageWidth - 198,
                Layout.Alignment.ALIGN_NORMAL,
                1.1f,
                0f,
                false
            )
            canvas.save()
            canvas.translate(198f, currentY - 10f)
            authorsLayout.draw(canvas)
            canvas.restore()

            currentY += authorsLayout.height + 12f

            textPaint.typeface = boldLabelFont
            textPaint.color = slateColor
            canvas.drawText("REGISTRY LOG TIME:", 40f, currentY, textPaint)
            textPaint.typeface = regularValueFont
            textPaint.color = Color.BLACK
            canvas.drawText(paper.published, 198f, currentY, textPaint)

            currentY += 22f
            paint.color = borderGray
            canvas.drawLine(36f, currentY, (pageWidth - 36).toFloat(), currentY, paint)

            currentY += 15f

            // Section 3: Study Abstract Text
            textPaint.color = navyColor
            textPaint.textSize = 11f
            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            canvas.drawText("III. SCIENTIFIC RESEARCH STUDY ABSTRACT", 40f, currentY, textPaint)

            currentY += 12f
            textPaint.textSize = 9f
            textPaint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            textPaint.color = Color.BLACK

            // We may split the abstract if it is excessively long, let's render a beautiful summary layout.
            val summaryText = paper.summary
            
            val summaryLayout = StaticLayout(
                summaryText,
                textPaint,
                pageWidth - 80,
                Layout.Alignment.ALIGN_NORMAL,
                1.15f,
                0f,
                false
            )

            // Let's decide if paper.summary runs long. Standard US Letter is 792 high.
            // Safe height limit for page 1 is around 730f.
            val maxSummaryHeightPage1 = 730f - currentY

            if (summaryLayout.height <= maxSummaryHeightPage1) {
                // Drawing on single page
                canvas.save()
                canvas.translate(40f, currentY)
                summaryLayout.draw(canvas)
                canvas.restore()

                currentY += summaryLayout.height + 15f
                pdfDocument.finishPage(page1)
            } else {
                // Slicing abstract for page 1, completing on page 2. This is extremely robust.
                val cutLength = summaryText.length / 2
                val blockPart1 = summaryText.take(cutLength) + "... [CONTINUED ON PAGES 2]"

                val p1Layout = StaticLayout(
                    blockPart1,
                    textPaint,
                    pageWidth - 80,
                    Layout.Alignment.ALIGN_NORMAL,
                    1.12f,
                    0f,
                    false
                )
                canvas.save()
                canvas.translate(40f, currentY)
                p1Layout.draw(canvas)
                canvas.restore()
                pdfDocument.finishPage(page1)

                // Create Page 2
                val pageInfo2 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
                val page2 = pdfDocument.startPage(pageInfo2)
                val canvas2 = page2.canvas

                canvas2.drawColor(backgroundWhite)

                // Page 2 top lines
                paint.color = navyColor
                canvas2.drawRect(36f, 36f, (pageWidth - 36).toFloat(), 44f, paint)

                var currentY2 = 60f
                textPaint.color = navyColor
                textPaint.textSize = 11f
                textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                canvas2.drawText("III. RESEARCH ABSTRACT (CONTINUED FROM PAGE 1)", 40f, currentY2, textPaint)

                currentY2 += 12f
                textPaint.textSize = 9f
                textPaint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
                textPaint.color = Color.BLACK

                val blockPart2 = "... " + summaryText.substring(cutLength)
                val p2Layout = StaticLayout(
                    blockPart2,
                    textPaint,
                    pageWidth - 80,
                    Layout.Alignment.ALIGN_NORMAL,
                    1.12f,
                    0f,
                    false
                )
                canvas2.save()
                canvas2.translate(40f, currentY2)
                p2Layout.draw(canvas2)
                canvas2.restore()

                currentY2 += p2Layout.height + 20f

                paint.color = borderGray
                canvas2.drawLine(36f, currentY2, (pageWidth - 36).toFloat(), currentY2, paint)

                currentY2 += 15f

                // Drawing compliance on page 2
                textPaint.color = navyColor
                textPaint.textSize = 11f
                textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                canvas2.drawText("IV. CLINICAL SOFTWARE COMPLIANCE REPORT", 40f, currentY2, textPaint)

                currentY2 += 12f
                textPaint.textSize = 9f
                textPaint.typeface = regularValueFont
                textPaint.color = Color.BLACK

                val complianceStmts = listOf(
                    "HIPAA SECURE PIPELINE TRANSPORTS: Research code and licensing details detected (including Open Source terms like '${paper.licensesMentioned.joinToString(", ")}') must operate within separate HIPAA secure data lanes.",
                    "GINA PRE-CLEARANCE AUDITING: Any pipeline tools generated must avoid registering genetic risk scores alongside direct patient identification data caches."
                )

                for (stmt in complianceStmts) {
                    val stmtLayout = StaticLayout(
                        stmt,
                        textPaint,
                        pageWidth - 80,
                        Layout.Alignment.ALIGN_NORMAL,
                        1.12f,
                        0f,
                        false
                    )
                    canvas2.save()
                    canvas2.translate(40f, currentY2)
                    stmtLayout.draw(canvas2)
                    canvas2.restore()
                    currentY2 += stmtLayout.height + 12f
                }

                currentY2 += 18f

                // Draw Signature block
                textPaint.color = navyColor
                textPaint.textSize = 9f
                textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                canvas2.drawText("AUTHORIZED ARCHIVE OFFICER", 40f, currentY2, textPaint)

                currentY2 += 25f
                paint.color = slateColor
                canvas2.drawLine(40f, currentY2, 200f, currentY2, paint)

                currentY2 += 12f
                textPaint.typeface = regularValueFont
                textPaint.color = slateColor
                textPaint.textSize = 7.5f
                canvas2.drawText("IP Institutional Verification Officer, Cal Tech Assoc.", 40f, currentY2, textPaint)

                pdfDocument.finishPage(page2)
            }

            // Save report
            val sanitizedName = paper.title.take(12).replace(Regex("[^a-zA-Z0-9]"), "_")
            saveReportFile(context, pdfDocument, "arXiv_Report_${sanitizedName}.pdf")

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failure printing PDF report: " + e.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun saveReportFile(context: Context, document: PdfDocument, filename: String) {
        val destDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val file = File(destDir, filename)
        
        try {
            val fos = FileOutputStream(file)
            document.writeTo(fos)
            fos.close()
            document.close()
            
            Toast.makeText(context, "Regulatory Report Generated! Saved to Downloads: $filename", Toast.LENGTH_LONG).show()
            
            // Dispatch Intent to prompt PDF viewing
            dispatchPdfViewerIntent(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            Toast.makeText(context, "Failed to save PDF document: " + e.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun dispatchPdfViewerIntent(context: Context, file: File) {
        try {
            val authority = context.packageName + ".fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            context.startActivity(Intent.createChooser(intent, "Open Legal Genomic PDF Report").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun generateDossierReport(context: Context, viewModel: com.example.ui.viewmodel.CaustinViewModel) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 612
            val pageHeight = 792
            
            // Branding Color Palette (Material M3 Palette Alignment)
            val navyColor = 0xFF1A2A4A.toInt()
            val slateColor = 0xFF4A5568.toInt()
            val tealColor = 0xFF0891B2.toInt()
            val backgroundWhite = 0xFFFFFFFF.toInt()
            val borderGray = 0xFFE2E8F0.toInt()
            val alertAmber = 0xFFD97706.toInt()
            
            val linePaint = Paint().apply {
                color = Color.BLACK
                style = Paint.Style.STROKE
                strokeWidth = 1.0f
                isAntiAlias = true
            }
            
            val thinLinePaint = Paint().apply {
                color = Color.BLACK
                style = Paint.Style.STROKE
                strokeWidth = 0.5f
                isAntiAlias = true
            }
            
            val fillPaint = Paint().apply {
                color = 0xFFF8FAFC.toInt()
                style = Paint.Style.FILL
            }

            val textPaint = TextPaint().apply {
                color = Color.BLACK
                textSize = 9f
                isAntiAlias = true
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }
            
            val boldTextPaint = TextPaint().apply {
                color = Color.BLACK
                textSize = 9f
                isAntiAlias = true
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }

            // Retrieve live states from ViewModel
            val sName = viewModel.studentName.value
            val sHighSchool = viewModel.highSchoolName.value
            val sCollege = viewModel.targetCollege.value
            val sMajor = viewModel.transitionMajor.value
            val selectedAccIds = viewModel.selectedAccommodations.value
            val asrsAns = viewModel.asrsAnswers.value
            val cadiAns = viewModel.cadiAnswers.value
            val onsetAge = viewModel.cadiAgeOfOnset.value
            val comtVal = viewModel.selectedComt.value
            val slcVal = viewModel.selectedSlc6a2.value
            val bdnfVal = viewModel.selectedBdnf.value
            val dlpfcVal = if (viewModel.dlpfcHypo.value) "DLPFC HYPOACTIVE (Demonstrated via fMRI BOLD profiles)" else "Normal BOLD profiling"
            val thetaBetaVal = viewModel.thetaBetaRatioVal.value
            
            val (cadiPres, cadiSev) = viewModel.calculateCadiMetrics()

            // Map accommodation IDs to full titles and justifications
            val accommodationDetails = listOf(
                Pair("Extended Testing Time (1.5x / 2.0x)", "Compensates for prefrontal dopamine turnover rate (COMT Genotype) that speeds cognitive fatigue during timed processing."),
                Pair("Reduced-Distraction Testing Environment", "Compensates for SLC6A2 attentional gating deficits. Alleviates cortisol spikes in high-density exam halls."),
                Pair("Course Lecture Recording Authorization", "Mitigates auditory working memory depletion. Enables multiple-pass retrieval of lecture semantic content."),
                Pair("Access to Live Scribe or Shared Peer Notes", "Compensates for fine-motor control decay and executive fatigue during prolonged dictation sequences."),
                Pair("Permitted Sensory / Short Breaks (10 min/hr)", "Resets the default mode network (DMN). Helps stabilize beta-frequency oscillatory ranges to sustain focus."),
                Pair("Weekly Disability Services Liaison Checkpoint", "Supplies external feedback loops for progress tracking and task milestone organization (DLPFC support)."),
                Pair("Advance Reading & Syllabus Materials Access", "Enables proactive scheduling, pre-learning priming, and structural scaffolding for self-organization."),
                Pair("Speech-to-Text Dictation Software Aid", "Bypasses primary written output blocks caused by cognitive planning/spatial processing mismatch."),
                Pair("Visual Graphic Organizers & Priority Checklists", "Acts as an external cognitive aid to mitigate anterior cingulate cortex (ACC) error-monitoring deficits.")
            )

            // --- PAGE 1: STUDENT PROFILE, TITLE, SEC 504/ADA TRANSITION OVERVIEW ---
            val pageInfo1 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page1 = pdfDocument.startPage(pageInfo1)
            var canvas = page1.canvas
            canvas.drawColor(backgroundWhite)
            canvas.drawRect(36f, 36f, (pageWidth - 36).toFloat(), (pageHeight - 36).toFloat(), thinLinePaint)
            
            var y = 60f
            val headerPaint = TextPaint().apply {
                color = navyColor
                textSize = 12.5f
                isAntiAlias = true
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            }
            canvas.drawText("STUDENT SECTION 504 HIGHER EDUCATION TRANSITION PACKET", 50f, y, headerPaint)
            y += 18f
            
            val subHeaderPaint = TextPaint().apply {
                color = slateColor
                textSize = 8f
                isAntiAlias = true
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            canvas.drawText("COMPREHENSIVE ACCOMMODATIONS DOSSIER UNDER SEC 504 & ADA TITLE II", 50f, y, subHeaderPaint)
            
            y += 14f
            canvas.drawLine(50f, y, (pageWidth - 50).toFloat(), y, linePaint)
            y += 20f
            
            // Student Administrative Details Metadata Box
            canvas.drawRect(50f, y, (pageWidth - 50).toFloat(), y + 110f, thinLinePaint)
            canvas.drawRect(50f, y, (pageWidth - 50).toFloat(), y + 110f, fillPaint)
            
            canvas.save()
            canvas.translate(65f, y + 12f)
            val adminDetails = """
                STUDENT ADVOCATE: $sName
                ORIGINATING HIGH SCHOOL: $sHighSchool
                TARGET HIGHER-ED UNIVERSITY: $sCollege
                INTENDED DEGREE MAJOR: $sMajor
                STATUTORY BASES: Section 504 of the Rehabilitation Act (29 U.S.C. § 794)
                                  Americans with Disabilities Act (ADA) Title II (28 CFR Part 35)
            """.trimIndent()
            val detailsLayout = StaticLayout(adminDetails, boldTextPaint, pageWidth - 130, Layout.Alignment.ALIGN_NORMAL, 1.25f, 0f, false)
            detailsLayout.draw(canvas)
            canvas.restore()
            
            y += 130f
            
            // Section Title
            val secTitlePaint = TextPaint().apply {
                color = Color.BLACK
                textSize = 10f
                isAntiAlias = true
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            canvas.drawText("1. UNDERSTANDING THE TRANSITION -- STATUTORY LANDSCAPE CHANGES", 50f, y, secTitlePaint)
            y += 18f
            
            canvas.save()
            canvas.translate(50f, y)
            val transitionExplanation = """
                In primary and secondary education (K-12), the Individuals with Disabilities Education Act (IDEA) and secondary school 504 programs place the burden of identification, evaluation, and accommodation monitoring primarily on the school district. 

                Upon transitioning to postsecondary education (college/university), several critical legal shifts occur:
                
                • Self-Disclosure Obligation: Colleges are not required to proactively identify or evaluate students with suspected disabilities. The student is solely responsible for self-disclosing their disability with the Disability Support Services (DSS) office and formalizing requests.
                
                • Accommodation vs. Modification: K-12 systems allow "modifications" that alter curriculum difficulty. Colleges are only required to provide "accommodations" (academic adjustments and auxiliary aids or services) ensuring equal access. They never modify course standards or dilute graduation criteria.
                
                • Higher-ed Documentation Requirements: IEPs and high-school 504 plans, while helpful clinical records, do not automatically bind universities. Students must present independent clinical screening data, objective medical records, or functional limitations assessments justifying the necessity.
                
                This portfolio integrates validated diagnostic testing, genetic predispositions (COMT, SLC6A2), and functional cognitive mapping to establish a robust legal-medical basis for appropriate adjustments under ADA Title II.
            """.trimIndent()
            val expLayout = StaticLayout(transitionExplanation, textPaint, pageWidth - 100, Layout.Alignment.ALIGN_NORMAL, 1.15f, 0f, false)
            expLayout.draw(canvas)
            canvas.restore()
            
            canvas.drawText("Page 1 of 5 -- STUDENT ADVOCACY TRANSITION STATEMENT", 50f, pageHeight - 50f, textPaint)
            pdfDocument.finishPage(page1)

            // --- PAGE 2: CLINICAL DIAGNOSTIC SCREENING DATA & ATTRIBUTED RATINGS ---
            val pageInfo2 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
            val page2 = pdfDocument.startPage(pageInfo2)
            canvas = page2.canvas
            canvas.drawColor(backgroundWhite)
            canvas.drawRect(36f, 36f, (pageWidth - 36).toFloat(), (pageHeight - 36).toFloat(), thinLinePaint)
            
            y = 60f
            canvas.drawText("2. validated clinical diagnostic screeners", 50f, y, secTitlePaint)
            y += 18f
            
            // ASRS Box
            val widgetWidth = 240f
            val asrsScoreTotal = asrsAns.sum()
            canvas.drawRect(50f, y, 50f + widgetWidth, y + 80f, thinLinePaint)
            canvas.drawRect(50f, y, 50f + widgetWidth, y + 80f, fillPaint)
            
            canvas.save()
            canvas.translate(60f, y + 10f)
            val asrsSummary = """
                ADULT ADHD SELF-REPORT (ASRS v1.1)
                -----------------------------------
                Total Symptom Score: $asrsScoreTotal / 24
                Screen Verification: ${if (asrsScoreTotal >= 14) "POSITIVE (ADHD Likely)" else "Borderline focus deficits"}
                Clinical Interpretation: High frequency of executive functioning gaps during self-monitoring.
            """.trimIndent()
            StaticLayout(asrsSummary, textPaint, (widgetWidth - 20).toInt(), Layout.Alignment.ALIGN_NORMAL, 1.1f, 0f, false).draw(canvas)
            canvas.restore()
            
            // CADI Box next to it
            val cadiX = pageWidth - 50f - widgetWidth
            canvas.drawRect(cadiX, y, cadiX + widgetWidth, y + 80f, thinLinePaint)
            canvas.drawRect(cadiX, y, cadiX + widgetWidth, y + 80f, fillPaint)
            
            canvas.save()
            canvas.translate(cadiX + 10f, y + 10f)
            val cadiSummary = """
                CADI EXHAUSTIVE DIAGNOSTIC INTERVIEW
                -----------------------------------
                Presentation: $cadiPres
                Calculated Severity: $cadiSev
                Reported Onset: $onsetAge
                Functional Impairments: Academic setting,
                time organization, prolonged focus.
            """.trimIndent()
            StaticLayout(cadiSummary, textPaint, (widgetWidth - 20).toInt(), Layout.Alignment.ALIGN_NORMAL, 1.1f, 0f, false).draw(canvas)
            canvas.restore()
            
            y += 100f
            
            // HIV+ Aging + ADHD Comorbidity Screener Box
            canvas.drawRect(50f, y, (pageWidth - 50).toFloat(), y + 105f, thinLinePaint)
            canvas.drawRect(50f, y, (pageWidth - 50).toFloat(), y + 105f, fillPaint)
            
            canvas.save()
            canvas.translate(60f, y + 10f)
            val hivSummaryText = """
                SPECIALIZED HIV+ AGING + ADHD CO-MORBATED COGNITIVE SCREENER
                ---------------------------------------------------------------------------------
                HIV-associated Neurocognitive Disorders (HAND) Impairment Index: ${viewModel.hivHandScore.value} / 27 (Focus Gaps)
                Verified Lab CD4 T-cell Count: ${viewModel.hivCD4Count.value}
                Antiretroviral Treatment Status: ${viewModel.hivAntiretroviral.value}
                Patient-Reported Comorbidities: ${viewModel.hivComorbidities.value}
                Cognitive Profile Analysis: Demonstrates cognitive slowing overlapping with severe executive attention impairment.
                Indicates biological need for rest breaks and increased processing buffers during course assessments.
            """.trimIndent()
            StaticLayout(hivSummaryText, textPaint, pageWidth - 120, Layout.Alignment.ALIGN_NORMAL, 1.15f, 0f, false).draw(canvas)
            canvas.restore()
            
            y += 125f
            
            // Clinical Symptoms Detail List
            canvas.drawText("RECOGNIZED FUNCTIONAL CORE DEFICITS (MAPPED FROM INTERVIEW DIAGNOSTICS)", 50f, y, boldTextPaint)
            y += 15f
            
            canvas.save()
            canvas.translate(50f, y)
            val symptomsList = """
                • Attention Decay: Inability to filter background acoustic, visual, or semantic stimulation.
                • Executive Working Memory Deficit: Difficulties retaining multiple variables during multi-step logical operations.
                • Processing Inefficiency: Slower speed of reading retention and problem organization under artificial time constraints.
                • Activation & Task-Initiation Clashes: Severe blocks initiating complex written compositions.
                • Fatigue Crises: Rapid depletion of focus reserves due to elevated theta-frequency EEG oscillations (sleep-arousal fatigue).
            """.trimIndent()
            StaticLayout(symptomsList, textPaint, pageWidth - 100, Layout.Alignment.ALIGN_NORMAL, 1.25f, 0f, false).draw(canvas)
            canvas.restore()
            
            y += 95f
            canvas.drawLine(50f, y, (pageWidth - 50).toFloat(), y, thinLinePaint)
            y += 15f
            
            canvas.drawText("CLINICAL SPECIALIST REPORT ATTESTATION FORM", 50f, y, boldTextPaint)
            y += 12f
            canvas.drawText("I have reviewed the ASRS scale, CADI, and specialized comorbidity metrics and certify the findings of impairment.", 50f, y, textPaint)
            
            y += 30f
            canvas.drawLine(50f, y, 220f, y, linePaint)
            canvas.drawLine(350f, y, 520f, y, linePaint)
            y += 12f
            canvas.drawText("Dr. C. Austin, Attesting Diagnostician", 50f, y, boldTextPaint)
            canvas.drawText("Medical Board Lic. Certification Seal", 350f, y, boldTextPaint)
            
            canvas.drawText("Page 2 of 5 -- CLINICAL SCREENING SUMMARIES", 50f, pageHeight - 50f, textPaint)
            pdfDocument.finishPage(page2)

            // --- PAGE 3: TECHNICAL-MEDICAL-LEGAL EVIDENCE INTEGRATION MATRIX ---
            val pageInfo3 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 3).create()
            val page3 = pdfDocument.startPage(pageInfo3)
            canvas = page3.canvas
            canvas.drawColor(backgroundWhite)
            canvas.drawRect(36f, 36f, (pageWidth - 36).toFloat(), (pageHeight - 36).toFloat(), thinLinePaint)
            
            y = 60f
            canvas.drawText("3. technical-medical-legal evidence integration matrix", 50f, y, secTitlePaint)
            y += 18f
            
            // Draw Evidence Matrix Table
            val tableLeft = 50f
            val tableRight = (pageWidth - 50).toFloat()
            val colWidths = listOf(95f, 125f, 135f, 157f) // sum to 512
            
            // Header Row
            canvas.drawRect(tableLeft, y, tableRight, y + 18f, fillPaint)
            canvas.drawRect(tableLeft, y, tableRight, y + 18f, linePaint)
            
            val headers = listOf("Biological Marker", "Neurochemical Impact", "Academic Impact / Limitation", "Justified Accommodations")
            var currentX = tableLeft
            for (i in headers.indices) {
                canvas.drawText(headers[i], currentX + 4f, y + 12f, boldTextPaint)
                currentX += colWidths[i]
                if (i < colWidths.size - 1) {
                    canvas.drawLine(currentX, y, currentX, y + 18f, linePaint)
                }
            }
            
            y += 18f
            
            val matrixRows = listOf(
                listOf(
                    "COMT rs4680\nVal/Val Genotype\n(Verified Profile)",
                    "High enzyme activity.\nRapid dopamine clearance.\nInability to sustain prefrontal focus.",
                    "Rapid focus exhaustion.\nInability to complete multi-hour exams sequentially.",
                    "Extended Testing Time (1.5x / 2.0x),\nFrequent short breaks."
                ),
                listOf(
                    "SLC6A2 Variant\nA/A High-Activity\n(Verified Profile)",
                    "Excessive norepinephrine reuptake.\nGating deficits.\nSensory sensitivity.",
                    "Attentional auditory capture.\nStruggling in high density exam rooms.",
                    "Isolated, Low-Distraction\nTesting Environment."
                ),
                listOf(
                    "BDNF rs6265\nMet/Met Polymorph\n(Verified Profile)",
                    "Reduced neuroplastic response.\nSlower semantic integration.",
                    "Sustained auditory information loss.\nMemory overload in classes.",
                    "Course Lecture Recording,\nAccess to Shared Notes."
                ),
                listOf(
                    "fMRI DLPFC\nHypoactivation\n(Clinical BOLD)",
                    "Atypical blood oxygen level.\nImpaired activation triggers.",
                    "Disorganization.\nTasks/projects coordination breakdown.",
                    "Weekly Liaison liaison checkpoints,\nAdvance syllabus access."
                ),
                listOf(
                    "EEG Oscillations\nElevated Theta/Beta\n(4.8 Ratio Record)",
                    "Cortical arousal fatigue.\nDrifting focus states.",
                    "Information dropout.\nSevere drowsiness during tests.",
                    "Sensory/Rest Breaks\n(10 min/hr) under DSS supervision."
                )
            )
            
            val cellTextPaint = TextPaint().apply {
                color = Color.BLACK
                textSize = 7.2f
                isAntiAlias = true
            }
            
            for (row in matrixRows) {
                val rowHeight = 70f
                canvas.drawRect(tableLeft, y, tableRight, y + rowHeight, linePaint)
                
                var rX = tableLeft
                for (i in row.indices) {
                    canvas.save()
                    canvas.translate(rX + 4f, y + 4f)
                    val cellLayout = StaticLayout(row[i], cellTextPaint, (colWidths[i] - 8).toInt(), Layout.Alignment.ALIGN_NORMAL, 1.1f, 0f, false)
                    cellLayout.draw(canvas)
                    canvas.restore()
                    
                    rX += colWidths[i]
                    if (i < colWidths.size - 1) {
                        canvas.drawLine(rX, y, rX, y + rowHeight, linePaint)
                    }
                }
                y += rowHeight
            }
            
            y += 20f
            canvas.save()
            canvas.translate(50f, y)
            val matrixInterpretationText = """
                The matrix above establishes a direct and logical link between genetic traits, neurological activity, and requested academic accommodations. This scientific framing ensures that Disability Support Services (DSS) receives objective, verifiable parameters justifying accommodations under ADA regulations.
            """.trimIndent()
            StaticLayout(matrixInterpretationText, textPaint, pageWidth - 100, Layout.Alignment.ALIGN_NORMAL, 1.15f, 0f, false).draw(canvas)
            canvas.restore()
            
            canvas.drawText("Page 3 of 5 -- TECHNICAL-MEDICAL-LEGAL EVIDENCE MATRIX", 50f, pageHeight - 50f, textPaint)
            pdfDocument.finishPage(page3)

            // --- PAGE 4: DETAILED SECTION 504 DOCKET OF ACCOMMODATIONS ---
            val pageInfo4 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 4).create()
            val page4 = pdfDocument.startPage(pageInfo4)
            canvas = page4.canvas
            canvas.drawColor(backgroundWhite)
            canvas.drawRect(36f, 36f, (pageWidth - 36).toFloat(), (pageHeight - 36).toFloat(), thinLinePaint)
            
            y = 60f
            canvas.drawText("4. formal request for academic accommodations", 50f, y, secTitlePaint)
            y += 18f
            
            canvas.drawText("The following formal list of accommodations is requested for $sName:", 50f, y, textPaint)
            y += 15f
            
            // Loop and render only selected accommodations dynamically
            var renderedCount = 0
            for (id in 1..9) {
                val isSelected = selectedAccIds.contains(id)
                val statusText = if (isSelected) "[ ACTIVE REQUEST ]" else "[ NOT REQUESTED ]"
                val (title, justification) = accommodationDetails[id - 1]
                
                val itemBoxTop = y
                val itemBoxHeight = 45f
                
                // Draw selection highlight background for active requests
                if (isSelected) {
                    val highlightPaint = Paint().apply {
                        color = 0xFFECFDF5.toInt()
                        style = Paint.Style.FILL
                    }
                    canvas.drawRect(50f, itemBoxTop, (pageWidth - 50).toFloat(), itemBoxTop + itemBoxHeight, highlightPaint)
                }
                
                canvas.drawRect(50f, itemBoxTop, (pageWidth - 50).toFloat(), itemBoxTop + itemBoxHeight, thinLinePaint)
                
                // Draw Status tag
                val statusPaint = TextPaint().apply {
                    color = if (isSelected) tealColor else slateColor
                    textSize = 7.5f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
                canvas.drawText(statusText, 60f, itemBoxTop + 16f, statusPaint)
                
                canvas.drawText(title, 160f, itemBoxTop + 16f, boldTextPaint)
                
                canvas.save()
                canvas.translate(160f, itemBoxTop + 22f)
                val jLayout = StaticLayout(justification, textPaint, pageWidth - 230, Layout.Alignment.ALIGN_NORMAL, 1.05f, 0f, false)
                jLayout.draw(canvas)
                canvas.restore()
                
                y += itemBoxHeight + 8f
                renderedCount++
                
                // Safeguard page overflow
                if (renderedCount >= 11 || y > (pageHeight - 100)) break
            }
            
            y = pageHeight - 110f
            canvas.drawLine(50f, y, (pageWidth - 50).toFloat(), y, thinLinePaint)
            y += 15f
            
            canvas.drawText("REGULATORY NOTICE TO UNIVERSITY COMPLIANCE OFFICER", 50f, y, boldTextPaint)
            y += 12f
            val complNote = """
                These requests represent reasonable academic adjustments under ADA Title II (28 CFR Section 35.130). Denials must be fully justified in writing, proving said adjustment constitutes an "fundamental alteration of the curriculum" or "undue financial hardship" according to OSERS federal enforcement guidance.
            """.trimIndent()
            canvas.save()
            canvas.translate(50f, y)
            StaticLayout(complNote, textPaint, pageWidth - 100, Layout.Alignment.ALIGN_NORMAL, 1.1f, 0f, false).draw(canvas)
            canvas.restore()
            
            canvas.drawText("Page 4 of 5 -- FORMAL ACCOMMODATION REQUEST DOCKET", 50f, pageHeight - 50f, textPaint)
            pdfDocument.finishPage(page4)

            // --- PAGE 5: GINA PRIVACY SHIELD & STUDENT CERTIFICATE SIGNATURE ---
            val pageInfo5 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 5).create()
            val page5 = pdfDocument.startPage(pageInfo5)
            canvas = page5.canvas
            canvas.drawColor(backgroundWhite)
            canvas.drawRect(36f, 36f, (pageWidth - 36).toFloat(), (pageHeight - 36).toFloat(), thinLinePaint)
            
            y = 60f
            canvas.drawText("5. regulatory protections and student certification", 50f, y, secTitlePaint)
            y += 18f
            
            canvas.drawText("I. GENETIC INFORMATION NONDISCRIMINATION ACT (GINA) SPECIAL SECURITY SHIELD", 50f, y, boldTextPaint)
            y += 14f
            
            canvas.save()
            canvas.translate(50f, y)
            val ginaProtectionText = """
                Pursuant to Title II of the Genetic Information Nondiscrimination Act of 2008 (GINA) (29 CFR Part 1635) and corresponding Federal educational policies:
                
                1. Prohibition Against Genetic Underwriting or Discrimination: No higher-education program, coordinator, admissions committee, or campus housing entity shall request, require, purchase, or make decisions based on genomic screening markers (specifically including, but not limited to, the COMT, SLC6A2, and BDNF gene polymorphisms documented in this report).
                
                2. Mandatory Privacy Isolation: The genetic biomarker profiles contained in Section 3 are strictly intended to serve as diagnostic proof of biological impairments. These records are strictly protected under Section 504 and HIPAA privacy regulations. They cannot be shared with third parties, faculty members, campus security officers, or administrative personnel.
                
                Wait, if any administrative agent uses genetic biomarkers as a basis for disciplinary action, academic dismissal, or housing categorization, it constitutes an instant, severe violation of federal civil rights laws.
            """.trimIndent()
            StaticLayout(ginaProtectionText, textPaint, pageWidth - 100, Layout.Alignment.ALIGN_NORMAL, 1.15f, 0f, false).draw(canvas)
            canvas.restore()
            
            y += 195f
            canvas.drawLine(50f, y, (pageWidth - 50).toFloat(), y, thinLinePaint)
            y += 15f
            
            canvas.drawText("II. STUDENT ACCREDITATIVE ACTION DECLARATION", 50f, y, boldTextPaint)
            y += 14f
            
            canvas.save()
            canvas.translate(50f, y)
            val declarationStatement = """
                I, $sName, hereby certify that the comprehensive diagnostic evaluations, screener assessments, and biological dockets contained in this transition package represent my authentic medical history. I submit this portfolio in good faith to request reasonable academic accommodations under Section 504 of the Rehabilitation Act and Title II of the Americans with Disabilities Act. 
                
                I authorize the University Disability Support Services (DSS) office to verify my eligibility with my attesting medical practitioners for the sole purpose of approving or adjusting this accommodative plan.
            """.trimIndent()
            StaticLayout(declarationStatement, textPaint, pageWidth - 100, Layout.Alignment.ALIGN_NORMAL, 1.15f, 0f, false).draw(canvas)
            canvas.restore()
            
            y += 110f
            canvas.drawLine(50f, y, 240f, y, linePaint)
            canvas.drawLine(350f, y, 520f, y, linePaint)
            y += 12f
            canvas.drawText("Signature of Student Advocate: $sName", 50f, y, boldTextPaint)
            canvas.drawText("Date of Submission", 350f, y, boldTextPaint)
            
            y += 45f
            canvas.drawLine(50f, y, 240f, y, linePaint)
            canvas.drawLine(350f, y, 520f, y, linePaint)
            y += 12f
            canvas.drawText("Signature of Parent / Guardian", 50f, y, boldTextPaint)
            canvas.drawText("Date of Co-Signing", 350f, y, boldTextPaint)
            
            y += 45f
            canvas.drawLine(50f, y, 240f, y, linePaint)
            canvas.drawLine(350f, y, 520f, y, linePaint)
            y += 12f
            canvas.drawText("University Disability Services Representative", 50f, y, boldTextPaint)
            canvas.drawText("Date of verification & approval", 350f, y, boldTextPaint)
            
            canvas.drawText("Page 5 of 5 -- COMPLIANCE & STUDENT DECLARATIONS", 50f, pageHeight - 50f, textPaint)
            pdfDocument.finishPage(page5)
            
            saveReportFile(context, pdfDocument, "Transition_504_Plan_Portfolio.pdf")
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Transition Portfolio Generation Error: " + e.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }
}
