package com.ducktapedapps.updoot.utils.moshiAdapters


import com.ducktapedapps.updoot.model.*
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import io.noties.markwon.Markwon
import javax.inject.Inject

class CommentListingAdapter @Inject constructor(private val markwon: Markwon) {
    private val moshi = Moshi.Builder().build()
    private val mapAdapter = moshi.adapter(Map::class.java)
    private val gildingsAdapter = moshi.adapter(Gildings::class.java)
    private val moreCommentsAdapter = moshi.adapter(MoreCommentData::class.java)

    @FromJson
    fun fromJson(postAndSubmission: List<Map<*, *>>): CommentListing =
            CommentListing(
                    if (postAndSubmission.size == 2) deserializeListing(postAndSubmission[1])
                    else listOf()
            )


    private fun deserializeListing(map: Map<*, *>): List<BaseComment> {
        val data = if (map["kind"] == "Listing") map["data"] as Map<*, *>
        else throw JsonDataException("UnExpected deserialize call for listing data type")

        val children = data["children"] as? List<Map<*, *>> ?: return listOf()

        val comments: MutableList<BaseComment> = children.filter { it["kind"] == "t1" }.map { deserializeCommentDetail(it) }.toMutableList()
        val moreComments = children.filter { it["kind"] == "more" }.map { moreCommentsAdapter.fromJson(mapAdapter.toJson(it["data"] as Map<*, *>)) }.firstOrNull()
        if (moreComments != null) comments += moreComments
        return comments
    }


    private fun deserializeCommentDetail(json: Map<*, *>): CommentData {
        val data = json["data"] as Map<*, *>

        // replies can be json obj or empty string
        val replies = data["replies"] as? Map<*, *>

        return CommentData(
                author = data["author"] as? String ?: "Unknown",
                body = markwon.toMarkdown(data["body"] as? String ?: ""),
                gildings = gildingsAdapter.fromJson(mapAdapter.toJson(data["gildings"] as Map<*, *>))
                        ?: Gildings(),
                id = data["id"] as String,
                ups = (data["ups"] as Double).toInt(),
                replies = if (replies != null) deserializeListing(replies) else listOf(),
                depth = (data["depth"] as? Double)?.toInt() ?: 0,
                repliesExpanded = false,
                is_submitter = data["is_submitter"] as? Boolean ?: false,
                likes = data["likes"] as Boolean?,
                parent_id = data["parent_id"] as String,
                name = data["name"] as String,
                author_flair_text = data["author_flair_text"] as String? ?: ""
        )
    }
}