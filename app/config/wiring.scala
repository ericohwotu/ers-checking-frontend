/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import akka.actor.ActorSystem
import play.api.Play
import services.AllWsHttp.runModeConfiguration
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.config.AppName
import uk.gov.hmrc.play.frontend.config.LoadAuditingConfig
import uk.gov.hmrc.play.http.ws._
import uk.gov.hmrc.play.http._

object ERSFileValidatorAuditConnector extends AuditConnector {
  override lazy val auditingConfig = LoadAuditingConfig("auditing")
}

trait WSHttp extends WSGet with HttpGet with HttpPatch with HttpPut with HttpDelete with HttpPost with WSPut with WSPost with WSDelete with WSPatch with AppName with HttpAuditing {
  protected def mode: play.api.Mode.Mode = Play.current.mode
  protected def runModeConfiguration: play.api.Configuration = Play.current.configuration
  protected def appNameConfiguration: play.api.Configuration = runModeConfiguration
  override protected def actorSystem : ActorSystem = Play.current.actorSystem
  override protected val configuration : scala.Option[com.typesafe.config.Config] = Option(runModeConfiguration.underlying)

  override val hooks = Seq(AuditingHook)
  override val auditConnector = ERSFileValidatorAuditConnector
}

object WSHttp extends WSHttp
