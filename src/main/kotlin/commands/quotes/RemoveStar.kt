package commands.quotes

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Updates
import commandhandler.CommandContext
import commands.base.BaseCommand
import database.collections.Quote
import database.quotesCollection
import ext.required
import ext.sendIncorrectQuote
import ext.useCommandProperly
import org.bson.conversions.Bson
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import type.CommandType.Quotes

class RemoveStar : BaseCommand(
    commandName = "removestar",
    commandDescription = "Star a quote",
    commandType = Quotes,
    commandArguments = mapOf("Quote ID | Message ID".required()),
    commandAliases = listOf("rs", "rsq", "unstar")
) {

    override fun execute(ctx: CommandContext) {
        super.execute(ctx)
        val args = ctx.args
        val contentIDRegex = "\\b\\d{18}\\b".toRegex()
        if (args.isNotEmpty()) {
            val message = args[0]
            when {
                message.matches(contentIDRegex) -> quotesCollection.removeStar(ctx, Quote::messageID eq message)
                message.toLongOrNull() != null -> quotesCollection.removeStar(ctx, Quote::quoteId eq message.toLong())
                else -> ctx.message.sendIncorrectQuote()
            }
        } else {
            ctx.message.useCommandProperly()
        }
    }

    private fun MongoCollection<Quote>.removeStar(ctx: CommandContext, filter: Bson) {
        val authorId = ctx.author.id
        val quote = findOne(filter)
        if (quote != null) {
            if (!quote.stars.contains(authorId)) {
                ctx.message.replyMsg("You don't have this quote starred!")
            } else {
                quotesCollection.updateOne(filter, Updates.pull("stars", authorId))
                ctx.message.replyMsg("Successfully removed star from quote #${quote.quoteId}")
            }
        } else {
            ctx.message.sendIncorrectQuote()
        }
    }

}
