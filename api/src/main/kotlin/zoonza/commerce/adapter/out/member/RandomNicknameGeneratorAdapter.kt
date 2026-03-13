package zoonza.commerce.adapter.out.member

import org.springframework.stereotype.Component
import zoonza.commerce.member.port.out.NicknameGenerator
import kotlin.random.Random

@Component
class RandomNicknameGeneratorAdapter : NicknameGenerator {
    override fun generate(): String {
        return RandomNicknameGenerator.generate()
    }
}

object RandomNicknameGenerator {
    private val adjectives = listOf(
        "씩씩한", "쾌활한", "용감한", "명랑한", "상냥한",
        "듬직한", "활발한", "씩씩한", "당당한", "솔직한",
        "유쾌한", "호기심", "느긋한", "침착한", "다정한",
        "귀여운", "멋있는", "빛나는", "포근한", "깜찍한",
        "반짝이는", "화려한", "단단한", "부드러운", "따뜻한",
        "재치있는", "똑똒한", "영리한", "지혜로운", "재빠른",
        "날쌘", "건강한", "튼튼한", "힘찬", "신나는",
        "춤추는", "노래하는", "달리는", "웃는", "행복한",
        "큰", "작은", "빠른", "하얀", "검은",
        "푸른", "붉은", "젊은", "굳센",
        "숲속의", "바다의", "하늘의", "달빛", "별빛",
        "새벽의", "노을빛", "안개속", "초원의", "계곡의",
        "엉뚱한", "황당한", "어리둥절", "헐레벌떡", "우당탕",
        "깜빡이는", "덜렁이는", "허둥대는", "투덜대는", "졸고있는",
    )

    private val animals = listOf(
        "판다", "호랑이", "사자", "여우", "늑대",
        "토끼", "다람쥐", "고양이", "강아지", "곰",
        "코끼리", "기린", "얼룩말", "캥거루", "코알라",
        "수달", "해달", "족제비", "오소리", "너구리",
        "고슴도치", "햄스터", "비버", "사슴", "순록",
        "앵무새", "독수리", "참새", "비둘기", "펭귄",
        "부엉이", "올빼미", "공작새", "플라밍고", "까치",
        "학", "두루미", "백조", "오리", "제비",
        "딱따구리", "카나리아", "앵무", "매", "솔개",
        "돌고래", "고래", "상어", "거북이", "해파리",
        "문어", "오징어", "게", "새우", "불가사리",
        "해마", "조개", "산호", "복어", "참치",
        "개구리", "도마뱀", "이구아나", "카멜레온", "뱀",
        "악어", "나비", "잠자리", "무당벌레", "반딧불이",
    )

    fun generate(): String {
        val adjective = adjectives.random()
        val animal = animals.random()
        val number = Random.nextInt(1, 10000)

        return adjective + animal + number
    }
}
