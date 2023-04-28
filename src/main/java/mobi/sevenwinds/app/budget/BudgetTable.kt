package mobi.sevenwinds.app.budget

import mobi.sevenwinds.app.budget.AuthorTable.created
import mobi.sevenwinds.app.budget.AuthorTable.name
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object BudgetTableWithAuthor : IntIdTable("budget") {
    val year = integer("year")
    val month = integer("month")
    val amount = integer("amount")
    val type = enumerationByName("type", 100, BudgetType::class)
    val authorId = integer("author_id")
}

class BudgetEntityWithAuthor(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<BudgetEntityWithAuthor>(BudgetTableWithAuthor)

    var year by BudgetTableWithAuthor.year
    var month by BudgetTableWithAuthor.month
    var amount by BudgetTableWithAuthor.amount
    var type by BudgetTableWithAuthor.type
    var authorId by BudgetTableWithAuthor.authorId

    fun toResponse(): BudgetRecord {
        return BudgetRecord(year, month, amount, type, authorId)
    }
}

object BudgetTableResponse : IntIdTable("budget") {
    val year = integer("year")
    val month = integer("month")
    val amount = integer("amount")
    val type = enumerationByName("type", 100, BudgetType::class)
    val authorName = name
    val authorCreated = created
}


class BudgetEntityResponse(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<BudgetEntityResponse>(BudgetTableResponse)

    private var year by BudgetTableResponse.year
    private var month by BudgetTableResponse.month
    private var amount by BudgetTableResponse.amount
    private var type by BudgetTableResponse.type
    private var authorName by  BudgetTableResponse.authorName
    private var created by BudgetTableResponse.authorCreated

    fun toResponse(): BudgetResponse {
        return BudgetResponse(year, month, amount, type, authorName, created)
    }
}


object AuthorTable : IntIdTable("author") {
    val name = text("full_name")
    val created = text("created")
}

class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorEntity>(AuthorTable)

    var name by AuthorTable.name
    var created by AuthorTable.created

    fun toResponse(): AuthorRecord {
        return AuthorRecord(name)
    }
}