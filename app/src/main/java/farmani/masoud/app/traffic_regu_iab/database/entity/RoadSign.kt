package farmani.masoud.app.traffic_regu_iab.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

import farmani.masoud.app.traffic_regu_iab.application.App
import farmani.masoud.app.traffic_regu_iab.database.profile.RoadSignProfile

/**
 * Created by Masoud Farmani on 8/24/2018.
 */

@Entity(tableName = "road_sign_table",
        foreignKeys = [
            ForeignKey(
                    entity = RoadSignGroup::class,
                    parentColumns = ["groupId"],
                    childColumns = ["groupId"],
                    onDelete = ForeignKey.CASCADE
            )])
data class RoadSign(val name: String// نام این تابلو
                    ,
                    val groupId: Char// شماره گروهی که این تابلو متعلق به آن است - محدودیت کلید خارجی
                    ,
                    val imageName: String,
                    @field:Embedded
                    val profile: RoadSignProfile) {
    @PrimaryKey(autoGenerate = true)
    var signId: Int = 0// مقدار این فیلد منحصر به هر تابلوست - خودکار مقداردهی می شود

    val imageId: Int
        get() = App.appResources
                .getIdentifier(imageName, "drawable", App.azeraPackageName)
}
