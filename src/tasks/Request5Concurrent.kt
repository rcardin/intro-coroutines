package tasks

import contributors.*
import kotlinx.coroutines.*

suspend fun loadContributorsConcurrent(service: GitHubService, req: RequestData): List<User> =
    coroutineScope {
        val repos = service
            .getOrgReposResponse(req.org)
            .also { logRepos(req, it) }
            .bodyList()

        repos.map { repo ->
            async(Dispatchers.Default) {
                service
                    .getRepoContributorsResponse(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
            }
        }.awaitAll().flatten().aggregate()
    }
