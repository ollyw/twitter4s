package com.danielasfregola.twitter4s
package http.clients.streaming.statuses

import com.danielasfregola.twitter4s.entities.enums.FilterLevel
import com.danielasfregola.twitter4s.entities.enums.Language.Language
import com.danielasfregola.twitter4s.entities.streaming.CommonStreamingMessage
import com.danielasfregola.twitter4s.entities.enums.FilterLevel.FilterLevel
import com.danielasfregola.twitter4s.http.clients.streaming.statuses.parameters._
import com.danielasfregola.twitter4s.http.clients.streaming.{ StreamingClient, TwitterStream }
import com.danielasfregola.twitter4s.util.{ ActorContextExtractor, Configurations }

import scala.concurrent.Future



trait TwitterStatusClient extends StreamingClient with Configurations with ActorContextExtractor {

  private val statusUrl = s"$statusStreamingTwitterUrl/$twitterVersion/statuses"

  /** Starts a streaming connection from Twitter's public API, filtered with the 'follow', 'track' and 'location' parameters.
    * Although all of those three params are optional, at least one must be specified.
    * The track, follow, and locations fields should be considered to be combined with an OR operator.
    * The function returns a future of a `TwitterStream` that can be use to close or replace the stream when needed.
    * If there are failures in establishing the initial connection, the Future returned will be completed with a failure.
    * Since it's an asynchronous event stream, all the events will be parsed as entities of type `CommonStreamingMessage`
    * and processed accordingly to the partial function `f`. All the messages that do not match `f` are automatically ignored.
    * For more information see
    * <a href="https://dev.twitter.com/streaming/reference/post/statuses/filter" target="_blank">
    *   https://dev.twitter.com/streaming/reference/post/statuses/filter</a>.
    *
    * @param follow : Empty by default. A comma separated list of user IDs, indicating the users to return statuses for in the stream.
    *                 For more information <a href="https://dev.twitter.com/streaming/overview/request-parameters#follow" target="_blank">
    *                   https://dev.twitter.com/streaming/overview/request-parameters#follow</a>
    * @param track : Empty by default. Keywords to track. Phrases of keywords are specified by a comma-separated list.
    *                For more information <a href="https://dev.twitter.com/streaming/overview/request-parameters#track" target="_blank">
    *                  https://dev.twitter.com/streaming/overview/request-parameters#track</a>
    * @param locations : Empty by default. Specifies a set of bounding boxes to track.
    *                    For more information <a href="https://dev.twitter.com/streaming/overview/request-parameters#locations" target="_blank">
    *                      https://dev.twitter.com/streaming/overview/request-parameters#locations</a>
    * @param languages : Empty by default. A comma separated list of 'BCP 47' language identifiers.
    *                    For more information <a href="https://dev.twitter.com/streaming/overview/request-parameters#language" target="_blank">
    *                      https://dev.twitter.com/streaming/overview/request-parameters#language</a>
    * @param stall_warnings : Default to false. Specifies whether stall warnings (`WarningMessage`) should be delivered as part of the updates.
    * @param filter_level : Default to None. Set the minimum value of the filter_level Tweet attribute required to be included in the stream.
    *                    For more information <a href="https://dev.twitter.com/streaming/overview/request-parameters#filter-level" target="_blank">
    *                      https://dev.twitter.com/streaming/overview/request-parameters#filter-level</a>
    * @param f : the function that defines how to process the received messages
    */
  def filterStatuses(follow: Seq[Long] = Seq.empty,
                     track: Seq[String] = Seq.empty,
                     locations: Seq[Double] = Seq.empty,
                     languages: Seq[Language] = Seq.empty,
                     stall_warnings: Boolean = false,
                     filter_level: FilterLevel = FilterLevel.None)(f: PartialFunction[CommonStreamingMessage, Unit]): Future[TwitterStream] = {
    require(follow.nonEmpty || track.nonEmpty || locations.nonEmpty, "At least one of 'follow', 'track' or 'locations' needs to be non empty")
    val parameters = StatusFilterParameters(follow, track, locations, languages, stall_warnings, filter_level)
    preProcessing()
    Post(s"$statusUrl/filter.json", parameters: Product).processStream(f)
  }

  @deprecated("use filterStatuses instead", "2.2")
  def getStatusesFilter(follow: Seq[Long] = Seq.empty,
                        track: Seq[String] = Seq.empty,
                        locations: Seq[Double] = Seq.empty,
                        languages: Seq[Language] = Seq.empty,
                        stall_warnings: Boolean = false)(f: PartialFunction[CommonStreamingMessage, Unit]): Future[TwitterStream] =
    filterStatuses(follow, track, locations, languages, stall_warnings)(f)


  /** Starts a streaming connection from Twitter's public API, which is a a small random sample of all public statuses.
    * The Tweets returned by the default access level are the same, so if two different clients connect to this endpoint, they will see the same Tweets.
    * The function returns a future of a `TwitterStream` that can be use to close or replace the stream when needed.
    * If there are failures in establishing the initial connection, the Future returned will be completed with a failure.
    * Since it's an asynchronous event stream, all the events will be parsed as entities of type `CommonStreamingMessage`
    * and processed accordingly to the partial function `f`. All the messages that do not match `f` are automatically ignored.
    * For more information see
    * <a href="https://dev.twitter.com/streaming/reference/get/statuses/sample" target="_blank">
    *   https://dev.twitter.com/streaming/reference/get/statuses/sample</a>.
    *
    * @param languages : Empty by default. A comma separated list of 'BCP 47' language identifiers.
    *                    For more information <a href="https://dev.twitter.com/streaming/overview/request-parameters#language" target="_blank">
    *                      https://dev.twitter.com/streaming/overview/request-parameters#language</a>
    * @param stall_warnings : Default to false. Specifies whether stall warnings (`WarningMessage`) should be delivered as part of the updates.
    * @param filter_level : Default to None. Set the minimum value of the filter_level Tweet attribute required to be included in the stream.
    *                    For more information <a href="https://dev.twitter.com/streaming/overview/request-parameters#filter-level" target="_blank">
    *                      https://dev.twitter.com/streaming/overview/request-parameters#filter-level</a>
    * @param f : the function that defines how to process the received messages
    */
  def sampleStatuses(languages: Seq[Language] = Seq.empty,
                     stall_warnings: Boolean = false,
                     filter_level: FilterLevel = FilterLevel.None)
                    (f: PartialFunction[CommonStreamingMessage, Unit]): Future[TwitterStream] = {
    val parameters = StatusSampleParameters(languages, stall_warnings, filter_level)
    preProcessing()
    Get(s"$statusUrl/sample.json", parameters).processStream(f)
  }

  @deprecated("use sampleStatuses instead", "2.2")
  def getStatusesSample(languages: Seq[Language] = Seq.empty,
                        stall_warnings: Boolean = false)
                       (f: PartialFunction[CommonStreamingMessage, Unit]): Future[TwitterStream] =
    sampleStatuses(languages, stall_warnings)(f)

  /** Starts a streaming connection from Twitter's firehose API of all public statuses.
    * Few applications require this level of access.
    * Creative use of a combination of other resources and various access levels can satisfy nearly every application use case.
    * For more information see <a href="https://dev.twitter.com/streaming/reference/get/statuses/firehose" target="_blank">
    *   https://dev.twitter.com/streaming/reference/get/statuses/firehose</a>.
    * The function returns a future of a `TwitterStream` that can be use to close or replace the stream when needed.
    * If there are failures in establishing the initial connection, the Future returned will be completed with a failure.
    * Since it's an asynchronous event stream, all the events will be parsed as entities of type `CommonStreamingMessage`
    * and processed accordingly to the partial function `f`. All the messages that do not match `f` are automatically ignored.
    *
    * @param count : Optional. The number of messages to backfill.
    *               For more information see <a href="https://dev.twitter.com/streaming/overview/request-parameters#count" target="_blank">
    *                 https://dev.twitter.com/streaming/overview/request-parameters#count</a>
    * @param languages : Empty by default. A comma separated list of 'BCP 47' language identifiers.
    *                    For more information <a href="https://dev.twitter.com/streaming/overview/request-parameters#language" target="_blank">
    *                      https://dev.twitter.com/streaming/overview/request-parameters#language</a>
    * @param stall_warnings : Default to false. Specifies whether stall warnings (`WarningMessage`) should be delivered as part of the updates.
    * @param filter_level : Default to None. Set the minimum value of the filter_level Tweet attribute required to be included in the stream.
    *                    For more information <a href="https://dev.twitter.com/streaming/overview/request-parameters#filter-level" target="_blank">
    *                      https://dev.twitter.com/streaming/overview/request-parameters#filter-level</a>
    * @param f : the function that defines how to process the received messages.
    */
  def firehoseStatuses(count: Option[Int] = None,
                       languages: Seq[Language] = Seq.empty,
                       stall_warnings: Boolean = false,
                       filter_level: FilterLevel = FilterLevel.None)
                      (f: PartialFunction[CommonStreamingMessage, Unit]): Future[TwitterStream] = {
    val maxCount = 150000
    require(Math.abs(count.getOrElse(0)) <= maxCount, s"count must be between -$maxCount and +$maxCount")
    val parameters = StatusFirehoseParameters(languages, count, stall_warnings, filter_level)
    preProcessing()
    Get(s"$statusUrl/firehose.json", parameters).processStream(f)
  }

  @deprecated("use firehoseStatuses instead", "2.2")
  def getStatusesFirehose(count: Option[Int] = None,
                          languages: Seq[Language] = Seq.empty,
                          stall_warnings: Boolean = false)
                         (f: PartialFunction[CommonStreamingMessage, Unit]): Future[TwitterStream] =
    firehoseStatuses(count, languages, stall_warnings)(f)
}
