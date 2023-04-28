package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object BudgetService {
    suspend fun addRecordWithAuthor(body: BudgetRecord, param: BudgetAuthorParam): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntityWithAuthor.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.authorId = param.authorId
            }
            return@transaction entity.toResponse()
        }
    }

    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntityWithAuthor.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.authorId = 0
            }
            return@transaction entity.toResponse()
        }
    }

    suspend fun addAuthor(body: AuthorRecord): AuthorRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = AuthorEntity.new {
                this.name = body.name
                this.created = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            }
            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val query = BudgetTableResponse
                .join(AuthorTable, JoinType.INNER, BudgetTableWithAuthor.authorId, AuthorTable.id)
                .select { BudgetTableResponse.year eq param.year }
                .limit(param.limit, param.offset)

            return@transaction BudgetYearStatsResponse(
                total = getTotal(query),
                totalByType = getTotalByType(query),
                items = getItems(query)
            )
        }
    }

    suspend fun getYearStatsWithName(param: BudgetYearParamName): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val query = BudgetTableResponse
                .join(AuthorTable, JoinType.INNER, BudgetTableWithAuthor.authorId, AuthorTable.id)
                .select { BudgetTableResponse.year eq param.year }
                .andWhere { BudgetTableResponse.authorName.lowerCase() eq param.name.toLowerCase() }
                .limit(param.limit, param.offset)

            return@transaction BudgetYearStatsResponse(
                total = getTotal(query),
                totalByType = getTotalByType(query),
                items = getItems(query)
            )
        }
    }

    private fun getTotal(query: Query): Int {
        return query.count()
    }

    private fun getTotalByType(query: Query): Map<String, Int> {
        val data = BudgetEntityResponse.wrapRows(query).map { it.toResponse() }
        return data.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }
    }

    private fun getItems(query: Query): List<BudgetResponse> {
        val data = BudgetEntityResponse.wrapRows(query).map { it.toResponse() }
        return data.sortedByDescending { it.amount }.sortedBy { it.month }
    }
}