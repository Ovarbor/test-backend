package mobi.sevenwinds.app.budget
import com.papsign.ktor.openapigen.annotations.parameters.PathParam
import com.papsign.ktor.openapigen.annotations.parameters.QueryParam
import com.papsign.ktor.openapigen.annotations.type.number.integer.max.Max
import com.papsign.ktor.openapigen.annotations.type.number.integer.min.Min
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route

fun NormalOpenAPIRoute.budget() {

    route("/budget") {
        route("/add/author/{authorId}").post<BudgetAuthorParam, BudgetRecord, BudgetRecord>(info("Добавить запись")) { param, body ->
            respond(BudgetService.addRecordWithAuthor(body, param))
        }
        route("/add").post<Unit, BudgetRecord, BudgetRecord>(info("Добавить запись")) { param, body ->
            respond(BudgetService.addRecord(body))
        }
        route("/year/{year}/stats") {
            get<BudgetYearParam, BudgetYearStatsResponse>(info("Получить статистику за год")) { param ->
                respond(BudgetService.getYearStats(param))
            }
        }
        route("/year/{year}/name/{name}/stats") {
            get<BudgetYearParamName, BudgetYearStatsResponse>(info("Получить статистику за год с фильтром по имени")) { param ->
                respond(BudgetService.getYearStatsWithName(param))
            }
        }
    }
    route("/author") {
        route("/add").post<Unit, AuthorRecord, AuthorRecord>(info("Добавить автора")) {param, body ->
            respond(BudgetService.addAuthor(body))
        }
    }
}

data class AuthorRecord (
     val name : String
)

data class BudgetRecord(
    @Min(1900) val year: Int,
    @Min(1) @Max(12) val month: Int,
    @Min(1) val amount: Int,
    val type: BudgetType,
    val authorId: Int
)

data class BudgetResponse (
    @Min(1900) val year: Int,
    @Min(1) @Max(12) val month: Int,
    @Min(1) val amount: Int,
    val type: BudgetType,
    val authorName: String,
    val created: String
)

data class BudgetYearParam(
    @PathParam("Год") val year: Int,
    @QueryParam("Лимит пагинации") val limit: Int,
    @QueryParam("Смещение пагинации") val offset: Int,
)

data class BudgetYearParamName(
    @PathParam("Год") val year: Int,
    @QueryParam("Лимит пагинации") val limit: Int,
    @QueryParam("Смещение пагинации") val offset: Int,
    @PathParam("Имя") val name: String
)

data class BudgetAuthorParam(
    @PathParam("Автор") val authorId: Int
)

class BudgetYearStatsResponse(
    val total: Int,
    val totalByType: Map<String, Int>,
    val items: List<BudgetResponse>,
)

enum class BudgetType {
    Приход, Расход
}