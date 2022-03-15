package com.smartbear.tcpbench.engines;

import com.smartbear.tcpbench.Query;
import com.smartbear.tcpbench.TcpEngine;
import com.smartbear.tcpbench.Verdict;
import com.smartesting.comet.ApiClient;
import com.smartesting.comet.ApiException;
import com.smartesting.comet.api.PrioritizationsApi;
import com.smartesting.comet.api.ProjectsApi;
import com.smartesting.comet.api.TestCyclesApi;
import com.smartesting.comet.api.TestsApi;
import com.smartesting.comet.auth.ApiKeyAuth;
import com.smartesting.comet.model.Prioritization;
import com.smartesting.comet.model.Project;
import com.smartesting.comet.model.Test;
import com.smartesting.comet.model.TestCycle;
import com.smartesting.comet.model.TestVerdict;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.smartbear.tcpbench.Env.getEnv;
import static com.smartesting.comet.Configuration.getDefaultApiClient;

public class Comet implements TcpEngine {
    private final ApiClient client;
    private String projectName;

    public Comet() {
        client = getDefaultApiClient();
        client.setBasePath(getEnv("COMET_URL"));
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) client.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey(getEnv("COMET_API_KEY"));
        client.setReadTimeout(500000); // 5 minutes
    }

    @Override
    public void prepare(String projectName) throws ApiException {
        this.projectName = projectName;
        ProjectsApi projectsApi = new ProjectsApi(client);

        try {
            projectsApi.deleteProject(projectName);
        } catch (ApiException ae) {
            // ignore
        }
        projectsApi.createProject(new Project().name(projectName));
    }

    @Override
    public void defineTestCycle(String testCycleId, List<Verdict> verdicts, Query query) {
        List<String> shas = query.getShas(testCycleId);
        Set<String> patches = shas.stream().flatMap(sha -> query.getModifiedFiles(sha, ".java$").stream()).collect(Collectors.toSet());
        int filesChanged = patches.size();

        List<Test> tests = verdicts.stream().map(verdict -> new Test().id(verdict.getTestId())).collect(Collectors.toList());
        TestCycle cycle = new TestCycle()
                .id(testCycleId)
                .filesChanged(filesChanged)
                .tests(tests);

        TestCyclesApi testCyclesApi = new TestCyclesApi(client);
        try {
            testCyclesApi.addTestCycle(projectName, cycle);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getOrdering(String testCycleId) {
        PrioritizationsApi prioritizationsApi = new PrioritizationsApi(client);
        Prioritization prioritization = null;
        try {
            prioritization = prioritizationsApi.prioritize(projectName, String.valueOf(testCycleId));
            return prioritization.getTests();
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void train(String testCycleId, List<Verdict> verdicts) {
        TestsApi testsApi = new TestsApi(client);
        List<TestVerdict> testVerdicts = verdicts.stream()
                .map(verdict -> new TestVerdict()
                        .id(verdict.getTestId())
                        .methodsNumber(verdict.getCount())
                        .duration(verdict.getDuration().toMillis() * 1000f)
                        .fail(verdict.isFailure()))
                .collect(Collectors.toList());
        try {
            testsApi.updateSuite(projectName, String.valueOf(testCycleId), testVerdicts);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

}
