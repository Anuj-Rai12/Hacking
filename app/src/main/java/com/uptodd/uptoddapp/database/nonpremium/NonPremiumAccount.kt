package com.uptodd.uptoddapp.database.nonpremium

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "non_premium_account_table")
data class NonPremiumAccount(
    @PrimaryKey(autoGenerate = true)
    var userId: Long = 0,

    @ColumnInfo(name = "name")
    var name: String? = null,

    @ColumnInfo(name = "kids_dob")
    var kidsDob: String? = null,

    @ColumnInfo(name = "kids_name")
    var kidsName: String? = null,

    @ColumnInfo(name = "kids_toy")
    var kidsToy: String? = null,

    @ColumnInfo(name = "minutes_baby")
    var minutesForBaby: Int? = null,

    @ColumnInfo(name = "mother_stage")
    var motherStage: String? = null,

    @ColumnInfo(name = "any_thing_special")
    var anythingSpecial: String? = null,

    @ColumnInfo(name = "major_objective")
    var majorObjective: String? = null,

    @ColumnInfo(name = "expected_month_of_delivery")
    var expectedMonthsOfDelivery: String? =null,

    @ColumnInfo(name = "anything_you_do")
    var anythingYouDo: String? = null,
    @ColumnInfo(name = "which_parent")
    var whichParent: String? = null
)