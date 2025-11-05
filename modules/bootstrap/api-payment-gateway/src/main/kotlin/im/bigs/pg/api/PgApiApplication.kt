package im.bigs.pg.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * API 실행 진입점. bootstrap 모듈은 실행/환경설정만을 담당합니다.
 */
@SpringBootApplication(scanBasePackages = ["im.bigs.pg"])
@EnableJpaRepositories(basePackages = ["im.bigs.pg.infra.persistence"])
@EntityScan(basePackages = ["im.bigs.pg"])
class PgApiApplication

fun main(args: Array<String>) {
    runApplication<PgApiApplication>(*args)
}
