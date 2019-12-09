package com.ducktapedapps.updoot.model

import java.io.Serializable

data class Images(val source: Source,
                  val resolutions: List<Resolution>) : Serializable
