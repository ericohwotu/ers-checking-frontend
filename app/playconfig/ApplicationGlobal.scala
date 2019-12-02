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

package playconfig

import com.typesafe.config.Config
import config.ErsContextImpl
import net.ceedubs.ficus.Ficus._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Request
import play.api.{Application, Configuration, Play}
import play.twirl.api.Html
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.play.config.{AppName, ControllerConfig, RunMode}
import uk.gov.hmrc.play.frontend.bootstrap.{DefaultFrontendGlobal, ShowErrorPage}
import uk.gov.hmrc.play.frontend.filters.{FrontendAuditFilter, FrontendLoggingFilter, MicroserviceFilterSupport}
import play.api.i18n.Lang

object ApplicationGlobal extends DefaultFrontendGlobal with RunMode with ShowErrorPage {
  protected def mode: play.api.Mode.Mode = Play.current.mode
  protected def runModeConfiguration: play.api.Configuration = Play.current.configuration

  override val auditConnector = ERSAuditConnector
  override val loggingFilter = ERSLoggingFilter
  override val frontendAuditFilter = ERSFrontendAuditFilter
  implicit lazy val ErsContext = Play.current.injector.instanceOf[config.ErsContext]

  private def lang(implicit request: Request[_]): Lang =
    Lang(request.cookies.get("PLAY_LANG").map(_.value).getOrElse("en"))

  override def onStart(app: Application) {
    super.onStart(app)
    new ApplicationCrypto(Play.current.configuration.underlying).verifyConfiguration()
  }

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: Request[_]): Html =
  views.html.global_error(pageTitle, heading, message)(request, applicationMessages)

  override def notFoundTemplate(implicit request: Request[_]): Html = {
    implicit val _: Lang = lang
    views.html.global_page_not_found()(request, applicationMessages, ErsContextImpl)
  }

  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig("microservice.metrics")

}

object ControllerConfiguration extends ControllerConfig {
  lazy val controllerConfigs = Play.current.configuration.underlying.as[Config]("controllers")
}

object ERSLoggingFilter extends FrontendLoggingFilter with MicroserviceFilterSupport{
  override def controllerNeedsLogging(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsLogging
}

object ERSFrontendAuditFilter extends FrontendAuditFilter with RunMode with AppName with MicroserviceFilterSupport{
  protected def mode: play.api.Mode.Mode = Play.current.mode
  protected def runModeConfiguration: play.api.Configuration = Play.current.configuration
  protected def appNameConfiguration: play.api.Configuration = runModeConfiguration

  override lazy val maskedFormFields = Seq("password")

  override lazy val applicationPort = None

  override lazy val auditConnector = ERSAuditConnector

  override def controllerNeedsAuditing(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsAuditing
}
