package org.telegram.divo.entity

/*
 * Social links of a profile may live in several places depending on the role and on how they were
 * saved: model fields, agency fields, agency.site (filled at registration) or the social network lists.
 * Empty strings must not shadow values stored elsewhere, so take the first non-blank one.
 */

val UserInfo.instagramLink: String
    get() = firstNotBlank(model?.instagramUrl, agency?.instagramUrl, socialNetworkLink("instagram"))

val UserInfo.tiktokLink: String
    get() = firstNotBlank(model?.tiktokUrl, agency?.tiktokUrl, socialNetworkLink("tiktok"))

val UserInfo.youtubeLink: String
    get() = firstNotBlank(model?.youtubeUrl, agency?.youtubeUrl, socialNetworkLink("youtube"))

val UserInfo.websiteLink: String
    get() = firstNotBlank(model?.websiteUrl, agency?.websiteUrl, agency?.site)

private fun UserInfo.socialNetworkLink(provider: String): String? =
    (userSocialNetworks + agency?.socialNetworks.orEmpty())
        .firstOrNull { it.link.isNotBlank() && (it.provider.contains(provider, ignoreCase = true) || it.link.contains("$provider.com", ignoreCase = true)) }
        ?.link

private fun firstNotBlank(vararg values: String?): String =
    values.firstOrNull { !it.isNullOrBlank() }.orEmpty()
