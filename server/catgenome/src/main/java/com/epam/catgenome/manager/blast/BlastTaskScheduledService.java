package com.epam.catgenome.manager.blast;

import com.epam.catgenome.entity.blast.BlastTask;
import com.epam.catgenome.entity.blast.TaskStatus;
import com.epam.catgenome.exception.BlastRequestException;
import com.epam.catgenome.manager.blast.dto.BlastRequestInfo;
import com.epam.catgenome.util.db.Filter;
import com.epam.catgenome.util.db.QueryParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.apache.commons.lang3.StringUtils.join;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlastTaskScheduledService {
    private final BlastTaskManager blastTaskManager;
    private final BlastRequestManager blastRequestManager;


    @Scheduled(fixedRateString = "${blast.update.status.rate:60000}")
    public void updateTaskStatuses() {
        List<String> statuses = new ArrayList<>();
        statuses.add(String.valueOf(TaskStatus.CREATED.getId()));
        statuses.add(String.valueOf(TaskStatus.SUBMITTED.getId()));
        statuses.add(String.valueOf(TaskStatus.RUNNING.getId()));

        Filter filter = new Filter("status", "in", "(" + join(statuses, ",") + ")");
        QueryParameters parameters = new QueryParameters();
        parameters.setFilters(Collections.singletonList(filter));
        List<BlastTask> tasks = blastTaskManager.loadAllTasks(parameters).getBlastTasks();
        tasks.forEach(t -> {
            try {
                BlastRequestInfo blastRequestInfo = blastRequestManager.getTaskStatus(t.getId());
                String status = blastRequestInfo.getStatus();
                if (!status.equals(t.getStatus().name())) {
                    t.setStatus(TaskStatus.valueOf(status));
                    t.setStatusReason(blastRequestInfo.getReason());
                    blastTaskManager.updateTask(t);
                }
            } catch (BlastRequestException e) {
                log.debug(e.getMessage());
            }
        });
    }
}
