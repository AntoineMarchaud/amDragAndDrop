package com.amarchaud.amdraganddrop.domain.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.amarchaud.amdraganddrop.domain.ApiOnePerson
import kotlinx.parcelize.Parcelize
import javax.annotation.Nullable

@Parcelize
@Entity(tableName = "people")
data class EntityOnePerson(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name= "position_db") var position_db : Int = 0,
    @ColumnInfo(name = "name") @Nullable var name: String? = null,
    @ColumnInfo(name = "position") @Nullable var position: String? = null,
    @ColumnInfo(name = "location") @Nullable var location: String? = null,
    @ColumnInfo(name = "pic") @Nullable var pic: String? = null,
    @ColumnInfo(name = "platform") @Nullable var platform: String? = null
) : Parcelable {
    constructor(apiOnePerson: ApiOnePerson) : this(
        name = apiOnePerson.name,
        position = apiOnePerson.position,
        location = apiOnePerson.location,
        pic = apiOnePerson.pic,
        platform = apiOnePerson.platform
    )
}