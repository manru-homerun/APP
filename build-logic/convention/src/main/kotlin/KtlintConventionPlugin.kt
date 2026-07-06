import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jlleitschuh.gradle.ktlint.KtlintExtension

class KtlintConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // 플러그인 적용
            pluginManager.apply("org.jlleitschuh.gradle.ktlint")

            // 설정 적용
            extensions.configure<KtlintExtension> {
                // 안드로이드 전용 규칙 사용
                android.set(true)

                // 결과 리포트 설정
                reporters {
                    reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
                    reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
                }
            }
        }
    }
}
