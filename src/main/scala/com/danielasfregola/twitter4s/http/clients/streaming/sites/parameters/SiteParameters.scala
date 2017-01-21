package com.danielasfregola.twitter4s.http.clients.streaming.sites.parameters

import com.danielasfregola.twitter4s.entities.enums.FilterLevel.FilterLevel
import com.danielasfregola.twitter4s.entities.enums.Language.Language
import com.danielasfregola.twitter4s.entities.enums.WithFilter.WithFilter
import com.danielasfregola.twitter4s.http.marshalling.Parameters

private[twitter4s] case class SiteParameters(follow: Seq[Long],
                                             `with`: WithFilter,
                                             replies: Option[String],
                                             stringify_friend_ids: Boolean,
                                             language: Seq[Language],
                                             stall_warnings: Boolean,
                                             filter_level: FilterLevel) extends Parameters
