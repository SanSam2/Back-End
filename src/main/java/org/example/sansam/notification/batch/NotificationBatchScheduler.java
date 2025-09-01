package org.example.sansam.notification.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class NotificationBatchScheduler {

    private final JobLauncher jobLauncher;

    private final Job deleteExpiredNotificationsJob;

//    @Qualifier("reviewRequestJob")
//    private final Job reviewRequestJob;

    /**
     * 매일 새벽 3시 만료 알림 삭제 Job 실행
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void runDeleteExpiredNotificationsJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis()) // 매 실행마다 유니크 파라미터
                    .toJobParameters();

            jobLauncher.run(deleteExpiredNotificationsJob, jobParameters);
            log.info("deleteExpiredNotificationsJob 실행 완료");
        } catch (Exception e) {
            log.error("deleteExpiredNotificationsJob 실행 실패", e);
        }
    }

//    @Scheduled(cron = "0 0 15 * * *")
//    public void runBroadcastJob() {
//        try {
//            JobParameters jobParameters = new JobParametersBuilder()
//                    .addLong("time", System.currentTimeMillis())
//                    .addString("content", "삐리리로로오리얘얘뻐")
//                    .toJobParameters();
//
//            jobLauncher.run(broadcastJob, jobParameters);
//            log.info("Broadcast 실행 완료");
//        } catch (Exception e) {
//            log.info("Broadcast 실행 실패", e);
//        }
//    }

//    /**
//     * 매일 오후 2시 리뷰 요청 Job 실행
//     */
//    @Scheduled(cron = "0 0 14 * * *")
//    public void runReviewRequestJob() {
//        try {
//            JobParameters jobParameters = new JobParametersBuilder()
//                    .addLong("time", System.currentTimeMillis())
//                    .toJobParameters();
//
//            jobLauncher.run(reviewRequestJob, jobParameters);
//            log.info("reviewRequestJob 실행 완료");
//        } catch (Exception e) {
//            log.error("reviewRequestJob 실행 실패", e);
//        }
//    }
}
