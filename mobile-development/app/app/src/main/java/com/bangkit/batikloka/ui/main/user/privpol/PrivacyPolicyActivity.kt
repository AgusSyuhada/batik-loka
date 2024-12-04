package com.bangkit.batikloka.ui.main.user.privpol

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.model.PrivacyPolicySection
import com.bangkit.batikloka.databinding.ActivityPrivacyPolicyBinding
import com.bangkit.batikloka.ui.adapter.PrivacyPolicyAdapter

class PrivacyPolicyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPrivacyPolicyBinding
    private lateinit var privacyPolicyAdapter: PrivacyPolicyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val privacyPolicySections = listOf(
            PrivacyPolicySection(
                "Introduction", """
                This Privacy Policy describes Our policies and procedures on the collection, use and disclosure of Your information when You use the Service and tells You about Your privacy rights and how the law protects You.
                
                We use Your Personal data to provide and improve the Service. By using the Service, You agree to the collection and use of information in accordance with this Privacy Policy.
            """.trimIndent()
            ),
            PrivacyPolicySection(
                "Definitions", """
                - Account: A unique account created for You to access our Service
                - Application: BatikLoka, the software program provided by the Company
                - Company: Team BatikLoka Bangkit Capstone Project 2024
                - Personal Data: Any information that relates to an identified or identifiable individual
            """.trimIndent()
            ),
            PrivacyPolicySection(
                "Types of Data Collected", """
                We may collect:
                - Email address
                - First name and last name
                - Usage Data (IP address, device information, browsing data)
                - Pictures and information from device's camera and photo library
            """.trimIndent()
            ),
            PrivacyPolicySection(
                "Use of Personal Data", """
                We may use Your Personal Data to:
                - Provide and maintain our Service
                - Manage Your Account
                - Contact You
                - Provide updates and offers
                - Analyze and improve our Service
            """.trimIndent()
            ),
            PrivacyPolicySection(
                "Data Sharing", """
                We may share Your personal information:
                - With Service Providers
                - During business transfers
                - With Affiliates
                - With business partners
                - With other users (when you interact publicly)
            """.trimIndent()
            ),
            PrivacyPolicySection(
                "Data Retention", """
                We retain Your Personal Data only as long as necessary for the purposes outlined in this Privacy Policy. We may retain data to:
                - Comply with legal obligations
                - Resolve disputes
                - Enforce our legal agreements
            """.trimIndent()
            ),
            PrivacyPolicySection(
                "Children's Privacy", """
                Our Service does not address anyone under 13. We do not knowingly collect personal information from children under 13. If we become aware of such collection, we will take steps to remove that information.
            """.trimIndent()
            ),
            PrivacyPolicySection(
                "Changes to Privacy Policy", """
                We may update our Privacy Policy from time to time. We will notify You of changes via email or in-app notice. You are advised to review this Privacy Policy periodically.
            """.trimIndent()
            ),
            PrivacyPolicySection(
                "Contact Information", """
                If you have any questions about this Privacy Policy, You can contact us by visiting:
                
                https://github.com/AgusSyuhada/batik-loka
            """.trimIndent()
            )
        )

        privacyPolicyAdapter = PrivacyPolicyAdapter(privacyPolicySections)
        binding.privacyPolicyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PrivacyPolicyActivity)
            adapter = privacyPolicyAdapter
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}