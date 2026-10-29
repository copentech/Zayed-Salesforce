package com.example.util

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.model.OrderWithItems
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CommissionPdfPrinter {
    fun printCommissionSlip(context: Context, orderWithItems: OrderWithItems) {
        val order = orderWithItems.order
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "KE")).apply {
            currency = java.util.Currency.getInstance("KES")
        }
        val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(
            if (order.commissionPaidAt > 0) Date(order.commissionPaidAt) else Date(order.createdAt)
        )

        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; color: #0f172a; }
                    .header { text-align: center; border-bottom: 2px solid #d4af37; padding-bottom: 10px; }
                    .title { color: #d4af37; font-size: 20px; font-weight: bold; }
                    .subtitle { font-size: 11px; color: #64748b; text-transform: uppercase; margin-top: 4px; }
                    .hero { background: #ecfdf5; border: 1px solid #34d399; border-radius: 8px; padding: 15px; text-align: center; margin: 15px 0; }
                    .amount { font-size: 28px; font-weight: bold; color: #065f46; }
                    .table { width: 100%; border-collapse: collapse; margin-top: 15px; }
                    .table td { padding: 8px; border-bottom: 1px solid #e2e8f0; font-size: 13px; }
                    .table td.label { color: #64748b; width: 40%; }
                    .table td.value { font-weight: bold; text-align: right; }
                    .stamp { background: #fffbeb; border: 1px solid #d4af37; padding: 10px; border-radius: 6px; text-align: center; margin-top: 15px; }
                    .footer { text-align: center; font-size: 10px; color: #94a3b8; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="title">ZAYED SALESFORCE</div>
                    <div class="subtitle">Official Commission Payout Slip • Owned by Al Yasir Bin Zayed</div>
                </div>

                <div class="hero">
                    <div style="font-size: 11px; color: #047857; font-weight: bold;">COMMISSION AMOUNT PAID</div>
                    <div class="amount">${currencyFormatter.format(order.commissionAmount)}</div>
                    <div style="font-size: 12px; color: #047857; margin-top: 4px;">Transaction Ref: ${order.commissionPaymentCode.ifEmpty { "TXN-APPROVED" }}</div>
                </div>

                <table class="table">
                    <tr><td class="label">Sales Representative:</td><td class="value">${order.accountHolderName}</td></tr>
                    <tr><td class="label">Representative Contact:</td><td class="value">${order.accountHolderContact}</td></tr>
                    <tr><td class="label">Linked Order Number:</td><td class="value">${order.orderNumber}</td></tr>
                    <tr><td class="label">Customer Business:</td><td class="value">${order.businessName}</td></tr>
                    <tr><td class="label">Customer Location:</td><td class="value">${order.customerLocation}</td></tr>
                    <tr><td class="label">Order Total Value:</td><td class="value">${currencyFormatter.format(order.totalAmount)}</td></tr>
                    <tr><td class="label">Commission Model:</td><td class="value">${if (order.commissionType == "FIXED") "Fixed Flat Rate" else "${String.format("%.1f", order.commissionRate * 100)}% Subtotal"}</td></tr>
                    <tr><td class="label">Approval & Payout Date:</td><td class="value">$dateStr</td></tr>
                </table>

                <div class="stamp">
                    <div style="font-size: 10px; font-weight: bold; color: #92400e;">OFFICIAL VERIFICATION TOKEN</div>
                    <div style="font-family: monospace; font-size: 14px; font-weight: bold; margin-top: 4px;">${order.stampVerificationCode}</div>
                    <div style="font-size: 9px; color: #78350f; margin-top: 2px;">Authenticated by Zayed SalesForce Enterprise System</div>
                </div>

                <div class="footer">
                    Executive Administration Approval • Official Physical Record Document
                </div>
            </body>
            </html>
        """.trimIndent()

        val webView = WebView(context).apply {
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                    val jobName = "Commission_Slip_${order.orderNumber}"
                    val printAdapter = view?.createPrintDocumentAdapter(jobName)
                    if (printAdapter != null) {
                        printManager.print(
                            jobName,
                            printAdapter,
                            PrintAttributes.Builder().build()
                        )
                    }
                }
            }
            loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        }
    }
}
