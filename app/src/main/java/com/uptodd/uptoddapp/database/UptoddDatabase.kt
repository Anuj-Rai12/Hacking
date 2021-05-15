package com.uptodd.uptoddapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.uptodd.uptoddapp.database.account.Account
import com.uptodd.uptoddapp.database.account.AccountDao
import com.uptodd.uptoddapp.database.activitysample.ActivitySample
import com.uptodd.uptoddapp.database.activitysample.ActivitySampleDao
import com.uptodd.uptoddapp.database.blogs.BlogCategories
import com.uptodd.uptoddapp.database.blogs.BlogCategoryDao
import com.uptodd.uptoddapp.database.blogs.BlogDao
import com.uptodd.uptoddapp.database.blogs.Blogs
import com.uptodd.uptoddapp.database.colour.ColourDao
import com.uptodd.uptoddapp.database.diet.DietDao
import com.uptodd.uptoddapp.database.expectedoutcome.ExpectedOutcomeDao
import com.uptodd.uptoddapp.database.media.music.MusicFiles
import com.uptodd.uptoddapp.database.media.music.MusicFilesDatabaseDao
import com.uptodd.uptoddapp.database.score.Score
import com.uptodd.uptoddapp.database.score.ScoreDatabaseDao
import com.uptodd.uptoddapp.database.stories.StoriesDao
import com.uptodd.uptoddapp.database.todo.Todo
import com.uptodd.uptoddapp.database.todo.TodoDatabaseDao
import com.uptodd.uptoddapp.database.todoApiDatabase.UpdateApi
import com.uptodd.uptoddapp.database.todoApiDatabase.UpdateApiDatabaseDao
import com.uptodd.uptoddapp.database.toys.ToysDao
import com.uptodd.uptoddapp.database.vaccination.VaccinationDao
import com.uptodd.uptoddapp.database.webinars.WebinarCategories
import com.uptodd.uptoddapp.database.webinars.WebinarCategoryDao
import com.uptodd.uptoddapp.database.webinars.Webinars
import com.uptodd.uptoddapp.database.webinars.WebinarsDatabaseDao
import com.uptodd.uptoddapp.database.yoga.YogaDao
import com.uptodd.uptoddapp.ui.otherScreens.otherScreens.color.Colour
import com.uptodd.uptoddapp.ui.otherScreens.otherScreens.diet.Diet
import com.uptodd.uptoddapp.ui.otherScreens.otherScreens.outcomes.ExpectedOutcomes
import com.uptodd.uptoddapp.ui.otherScreens.otherScreens.stories.Story
import com.uptodd.uptoddapp.ui.otherScreens.otherScreens.toy.Toy
import com.uptodd.uptoddapp.ui.otherScreens.otherScreens.vaccination.Vaccination
import com.uptodd.uptoddapp.ui.otherScreens.otherScreens.yoga.allYogas.Yoga

@Database(
    entities = [Score::class, Todo::class, UpdateApi::class,
        MusicFiles::class,
        Blogs::class, BlogCategories::class,
        Webinars::class, WebinarCategories::class,
        Vaccination::class, Toy::class, Story::class,
        ExpectedOutcomes::class, Yoga::class, Diet::class,
        Colour::class, Account::class, ActivitySample::class],
    version = 4,
    exportSchema = false
)

abstract class UptoddDatabase : RoomDatabase() {

    companion object {

        @Volatile
        private lateinit var uptoddDatabase: UptoddDatabase

        fun getInstance(context: Context): UptoddDatabase {
            synchronized(this) {
                if (!this::uptoddDatabase.isInitialized) {
                    uptoddDatabase = Room.databaseBuilder(
                        context.applicationContext,
                        UptoddDatabase::class.java,
                        "Uptodd Database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
                return uptoddDatabase
            }
        }
    }


    /* Add Dao's here */

    // account dao
    abstract val accountDatabaseDao: AccountDao

    // score dao
    abstract val scoreDatabaseDao: ScoreDatabaseDao

    // todo dao
    abstract val todoDatabaseDao: TodoDatabaseDao

    // updateApi dao
    abstract val updateApiDatabaseDao: UpdateApiDatabaseDao


    // music doa
    abstract val musicDatabaseDao: MusicFilesDatabaseDao

    // blog daos
    abstract val blogDao: BlogDao
    abstract val categoryDao: BlogCategoryDao

    // webinar dao
    abstract val webinarsDatabaseDao: WebinarsDatabaseDao
    abstract val webinarCategoryDao: WebinarCategoryDao

    // vaccination dao
    abstract val vaccinationDao: VaccinationDao

    // toys dao
    abstract val toysDatabaseDao: ToysDao

    // stories dao
    abstract val storiesDao: StoriesDao

    // expected outcomes dao
    abstract val expectedOutcomeDao: ExpectedOutcomeDao

    // diet dao
    abstract val dietDao: DietDao

    // colour dao
    abstract val colourDao: ColourDao

    // yoga dao
    abstract val yogaDao: YogaDao

    // activity sample dao
    abstract val activitySampleDao: ActivitySampleDao


    // provide migration here if any

}