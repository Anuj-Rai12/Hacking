package com.uptodd.uptoddapp.database.account

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account_table")
data class Account(
    @PrimaryKey(autoGenerate = true)
    var accountId: Long = 0,

    @ColumnInfo(name = "profile_image_url")
    var profileImageURL: String? = null,

    @ColumnInfo(name = "name")
    var name: String? = null,

    @ColumnInfo(name = "email")
    var email: String? = null,

    @ColumnInfo(name = "phone")
    var phone: String? = null,

    @ColumnInfo(name = "address")
    var address: String? = null,

    @ColumnInfo(name = "score")
    var score: Int = 0,

    @ColumnInfo(name = "total_score")
    var totalScore: Int = 0,

    @ColumnInfo(name = "is_nanny_mode")
    var isNannyMode: Boolean = false,

    @ColumnInfo(name = "nanny_mode_user_id")
    var nannyModeUserID: String? = null,

    @ColumnInfo(name = "nanny_mode_password")
    var nannyModePassword: String? = null,

    @ColumnInfo(name = "financial_mail_id")
    var financeMailId: String? = null,

    @ColumnInfo(name = "kids_dob")
    var kidsDob: String? = null,

    @ColumnInfo(name = "kids_name")
    var kidsName: String? = null,

    @ColumnInfo(name = "kids_gender")
    var kidsGender: String? = null,

    @ColumnInfo(name = "kids_photo")
    var kidsPhoto: String? = null,

    @ColumnInfo(name = "which_parent")
    var whichParent: String? = null,

    @ColumnInfo(name = "mother_stage")
    var motherStage: String? = null,

    @ColumnInfo(name = "free_session_available")
    var freeSessionAvailable: Long = 0,

    @ColumnInfo(name = "paid_session_count")
    var paidSessionCount: Long = 0,

    @ColumnInfo(name = "current_subscribed_plan")
    var currentSubscribedPlan: Long = 0,

    @ColumnInfo(name = "subscription_active")
    var subscriptionActive: Boolean = false,

    @ColumnInfo(name = "subscription_start_date")
    var subscriptionStartDate: String? = null

)