package mobi.sevenwinds.app.budget
import io.restassured.RestAssured
import mobi.sevenwinds.common.ServerTest
import mobi.sevenwinds.common.jsonBody
import mobi.sevenwinds.common.toResponse
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Assert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class BudgetApiKtTest : ServerTest() {

    @AfterEach
    internal fun setUp() {
        transaction { BudgetTableResponse.deleteAll() }
        transaction { AuthorTable.deleteWhere { AuthorTable.id.greater(3) } }
    }

    @Test
    fun testBudgetPagination() {

        addRecord(BudgetRecord(2020, 5, 10, BudgetType.Приход, 0))
        addRecord(BudgetRecord(2020, 5, 5, BudgetType.Приход, 0))
        addRecord(BudgetRecord(2020, 5, 20, BudgetType.Приход, 0))
        addRecord(BudgetRecord(2020, 5, 30, BudgetType.Приход, 0))
        addRecord(BudgetRecord(2020, 5, 40, BudgetType.Приход, 0))
        addRecord(BudgetRecord(2030, 1, 1, BudgetType.Расход, 0))

        RestAssured.given()
            .queryParam("limit", 5)
            .queryParam("offset", 0)
            .get("/budget/year/2020/stats")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println("${response.total} / ${response.items} / ${response.totalByType}")

                Assert.assertEquals(5, response.total)
                Assert.assertEquals(5, response.items.size)
                Assert.assertEquals(105, response.totalByType[BudgetType.Приход.name])
            }
    }

    @Test
    fun testStatsSortOrder() {
        addRecord(BudgetRecord(2020, 5, 100, BudgetType.Приход, 0))
        addRecord(BudgetRecord(2020, 1, 5, BudgetType.Приход, 0))
        addRecord(BudgetRecord(2020, 5, 50, BudgetType.Приход, 0))
        addRecord(BudgetRecord(2020, 1, 30, BudgetType.Приход, 0))
        addRecord(BudgetRecord(2020, 5, 400, BudgetType.Приход, 0))

        // expected sort order - month ascending, amount descending

        RestAssured.given()
            .get("/budget/year/2020/stats?limit=100&offset=0")
                .toResponse<BudgetYearStatsResponse>().let { response ->
                println(response.items)

                Assert.assertEquals(30, response.items[0].amount)
                Assert.assertEquals(5, response.items[1].amount)
                Assert.assertEquals(400, response.items[2].amount)
                Assert.assertEquals(100, response.items[3].amount)
                Assert.assertEquals(50, response.items[4].amount)
            }
    }

    @Test
    fun testInvalidMonthValues() {
        RestAssured.given()
            .jsonBody(BudgetRecord(2020, -5, 5, BudgetType.Приход, 0))
            .post("/budget/add")
            .then().statusCode(400)

        RestAssured.given()
            .jsonBody(BudgetRecord(2020, 15, 5, BudgetType.Приход, 0))
            .post("/budget/add")
            .then().statusCode(400)
    }



    @Test
    fun testBudgetFilterName() {

        addAuthor(AuthorRecord("ФёдоР ДоСтОевский"))
        addAuthor(AuthorRecord("ЛеВ Толстой"))
        addAuthor(AuthorRecord("Петр первый"))

        addRecordWithAuthor(BudgetRecord(2020, 5, 10, BudgetType.Приход, 1))
        addRecordWithAuthor(BudgetRecord(2020, 5, 5, BudgetType.Приход, 2))
        addRecordWithAuthor(BudgetRecord(2020, 5, 20, BudgetType.Приход, 1))
        addRecordWithAuthor(BudgetRecord(2020, 5, 30, BudgetType.Приход, 3))
        addRecordWithAuthor(BudgetRecord(2020, 5, 40, BudgetType.Приход, 2))
        addRecordWithAuthor(BudgetRecord(2030, 1, 1, BudgetType.Расход, 2))

        RestAssured.given()
            .queryParam("limit", 5)
            .queryParam("offset", 0)
            .param("name", "фёдор ДоСТОевский")
            .get("/budget/year/2020/name/фёдор ДоСТОевский/stats")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println("${response.total} / ${response.items} / ${response.totalByType}")

                Assert.assertEquals(2, response.total)
                Assert.assertEquals(2, response.items.size)
                Assert.assertEquals(30, response.totalByType[BudgetType.Приход.name])
                Assert.assertEquals("фёдор достоевский", response.items[1].authorName.toLowerCase())
            }
    }

    private fun addRecord(record: BudgetRecord) {
        RestAssured.given()
            .jsonBody(record)
            .post("/budget/add")
            .toResponse<BudgetRecord>().let { response ->
                Assert.assertEquals(record, response)
            }
    }

    private fun addRecordWithAuthor(record: BudgetRecord) {
        RestAssured.given()
            .jsonBody(record)
            .post("/budget/add/author/" + record.authorId)
            .toResponse<BudgetRecord>().let { response ->
                Assert.assertEquals(record, response)
            }
    }

    private fun addAuthor(record: AuthorRecord) {
        RestAssured.given()
            .jsonBody(record)
            .post("/author/add")
            .toResponse<AuthorRecord>().let { response ->
                Assert.assertEquals(record, response)
            }
    }
}