package me.prokhorov.coinfolio.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.prokhorov.coinfolio.domain.model.Portfolio
import me.prokhorov.coinfolio.domain.model.ValuedHolding
import me.prokhorov.coinfolio.domain.result.AppError
import me.prokhorov.coinfolio.domain.result.Resource
import me.prokhorov.coinfolio.domain.testing.FakeCoinRepository
import me.prokhorov.coinfolio.domain.testing.FakeHoldingRepository
import me.prokhorov.coinfolio.domain.testing.createCoin
import me.prokhorov.coinfolio.domain.testing.createHolding
import org.junit.Test

private const val BITCOIN = "bitcoin"
private const val ETHEREUM = "ethereum"

class GetPortfolioUseCaseTest {

    private val holdingRepository = FakeHoldingRepository()
    private val coinRepository = FakeCoinRepository()

    private val getPortfolio = GetPortfolioUseCase(
        GetHoldingsUseCase(holdingRepository),
        GetCoinsByIdsUseCase(coinRepository),
    )

    @Test
    fun `no holdings gives an empty portfolio`() = runTest {
        val portfolio = loadPortfolio()

        assertThat(portfolio.valuedHoldings).isEmpty()
        assertThat(portfolio.totalValue).isEqualToIgnoringScale("0")
    }

    @Test
    fun `every holding becomes a row with its coin`() = runTest {
        setMarketCoins(BITCOIN, ETHEREUM)
        holdingRepository.saveHolding(createHolding(BITCOIN, "0.5"))
        holdingRepository.saveHolding(createHolding(ETHEREUM, "2"))

        val portfolio = loadPortfolio()

        assertThat(portfolio.valuedHoldings).containsExactly(
            ValuedHolding(createHolding(BITCOIN, "0.5"), createCoin(BITCOIN)),
            ValuedHolding(createHolding(ETHEREUM, "2"), createCoin(ETHEREUM)),
        ).inOrder()
    }

    @Test
    fun `the total is the sum of quantity times price`() = runTest {
        coinRepository.marketCoins = mapOf(
            BITCOIN to createCoin(BITCOIN, price = "100"),
            ETHEREUM to createCoin(ETHEREUM, price = "25.5"),
        )
        holdingRepository.saveHolding(createHolding(BITCOIN, "0.5"))
        holdingRepository.saveHolding(createHolding(ETHEREUM, "2"))

        val portfolio = loadPortfolio()

        assertThat(portfolio.totalValue).isEqualToIgnoringScale("101")
    }

    // The user holds a coin the market answer does not carry, a delisted one for example.
    @Test
    fun `a coin the market does not know has no value and is left out of the total`() = runTest {
        setMarketCoins(BITCOIN)

        holdingRepository.saveHolding(createHolding(BITCOIN, "1"))
        holdingRepository.saveHolding(createHolding("dead-coin", "5"))

        val portfolio = loadPortfolio()

        assertThat(portfolio.valuedHoldings.last().coin).isNull()
        assertThat(portfolio.valuedHoldings.last().value).isNull()
        assertThat(portfolio.totalValue).isEqualToIgnoringScale("100")
    }

    @Test
    fun `a coin with no price has no value and is left out of the total`() = runTest {
        coinRepository.marketCoins = mapOf(
            BITCOIN to createCoin(BITCOIN, price = "100"),
            ETHEREUM to createCoin(ETHEREUM).copy(currentPrice = null),
        )
        holdingRepository.saveHolding(createHolding(BITCOIN, "1"))
        holdingRepository.saveHolding(createHolding(ETHEREUM, "3"))

        val portfolio = loadPortfolio()

        assertThat(portfolio.valuedHoldings.last().value).isNull()
        assertThat(portfolio.totalValue).isEqualToIgnoringScale("100")
    }

    @Test
    fun `a failure to read the holdings is a failure`() = runTest {
        holdingRepository.error = AppError.Storage

        val result = getPortfolio().first()

        assertThat(result).isEqualTo(Resource.Failure(AppError.Storage))
    }

    @Test
    fun `a failure to load the prices is a failure`() = runTest {
        coinRepository.marketError = AppError.RateLimited
        holdingRepository.saveHolding(createHolding(BITCOIN, "1"))

        val result = getPortfolio().first()

        assertThat(result).isEqualTo(Resource.Failure(AppError.RateLimited))
    }

    // The flow lives as long as the screen. A refresh should cost one request, so the later
    // results, which come from a change of the holdings, must not ask for fresh prices again.
    @Test
    fun `a refresh asks for fresh prices only once`() = runTest {
        setMarketCoins(BITCOIN)
        holdingRepository.saveHolding(createHolding(BITCOIN, "1"))

        getPortfolio(forceRefresh = true).test {
            awaitItem()
            assertThat(coinRepository.lastForceRefresh).isTrue()

            holdingRepository.saveHolding(createHolding(BITCOIN, "2"))

            awaitItem()
            assertThat(coinRepository.lastForceRefresh).isFalse()
        }
    }

    @Test
    fun `a normal load does not ask for fresh prices`() = runTest {
        setMarketCoins(BITCOIN)
        holdingRepository.saveHolding(createHolding(BITCOIN, "1"))

        getPortfolio().first()

        assertThat(coinRepository.lastForceRefresh).isFalse()
    }

    private fun setMarketCoins(vararg ids: String) {
        coinRepository.marketCoins = ids.associateWith(::createCoin)
    }

    private suspend fun loadPortfolio(): Portfolio {
        val result = getPortfolio().first()

        assertThat(result).isInstanceOf(Resource.Success::class.java)
        return (result as Resource.Success).data
    }
}
