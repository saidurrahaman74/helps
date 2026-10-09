package com.srcreation.smarthelp

import java.util.Calendar

data class DailyTip(
    val id: Int,
    val titleEn: String,
    val titleBn: String,
    val bodyEn: String,
    val bodyBn: String,
    val categoryEn: String,
    val categoryBn: String,
    val iconName: String = "Lightbulb"
)

object DailyTipsRepository {

    private val categoryMappings = listOf(
        "Nutrition & Diet" to "পুষ্টি ও খাদ্যভ্যাস",
        "Hydration & Water" to "পানি ও হাইড্রেশন",
        "Fitness & Movement" to "ফিটনেস ও ব্যায়াম",
        "Mental Health" to "মানসিক স্বাস্থ্য",
        "Sleep & Rest" to "ঘুম ও বিশ্রাম",
        "Heart & Pressure" to "হৃদস্বাস্থ্য ও প্রেশার",
        "Immunity & Wellness" to "রোগ প্রতিরোধ ক্ষমতা",
        "Digestive Health" to "হজম ও পরিপাকতন্ত্র",
        "Skin & Hygiene" to "ত্বক ও পরিচ্ছন্নতা",
        "Eye & Dental Care" to "চোখ ও দাঁতের যত্ন",
        "Women & Child Care" to "নারী ও শিশু স্বাস্থ্য",
        "Senior Vitality" to "জ্যেষ্ঠ স্বাস্থ্য ও দীর্ঘায়ু"
    )

    // Base hand-crafted curated premium tips covering key domains
    private val curatedBaseTips = listOf(
        DailyTip(
            1,
            "Start Day with Warm Water",
            "সকালে ইষদুষ্ণ পানি দিয়ে দিন শুরু করুন",
            "Drinking 1-2 glasses of warm water on an empty stomach activates internal organs, aids digestion, and flushes out daily toxins.",
            "সকালে খালি পেটে ১-২ গ্লাস কুসুম গরম পানি খেলে পরিপাকতন্ত্র সক্রিয় হয়, কোষ্ঠকাঠিন্য দূর হয় এবং শরীর থেকে টক্সিন বের হয়ে যায়।",
            "Hydration & Water", "পানি ও হাইড্রেশন", "WaterDrop"
        ),
        DailyTip(
            2,
            "Follow the 20-20-20 Eye Rule",
            "চোখের ক্লান্তি কমাতে ২০-২০-২০ নিয়ম",
            "Every 20 minutes of screen use, look at something 20 feet away for 20 seconds to prevent digital eye strain and dry eyes.",
            "মোবাইল বা কম্পিউটারে কাজ করার সময় প্রতি ২০ মিনিট পর ২০ ফুট দূরত্বের কোনো জিনিসের দিকে ২০ সেকেন্ড তাকিয়ে থাকুন। এতে চোখের ক্লান্তি কমে।",
            "Eye & Dental Care", "চোখ ও দাঁতের যত্ন", "Visibility"
        ),
        DailyTip(
            3,
            "Walk 10 Minutes After Meals",
            "খাবারের পর ১০ মিনিটের হাঁটা",
            "A gentle 10-minute walk after lunch or dinner helps regulate post-meal blood sugar spikes and promotes efficient digestion.",
            "দুপুর বা রাতের খাবারের পর ১০ মিনিট হালকা হাঁটাচলা করলে রক্তে গ্লুকোজের মাত্রা নিয়ন্ত্রণে থাকে এবং খাবার দ্রুত হজম হয়।",
            "Fitness & Movement", "ফিটনেস ও ব্যায়াম", "DirectionsRun"
        ),
        DailyTip(
            4,
            "Reduce Extra Table Salt",
            "খাবারে বাড়তি কাঁচা লবণ ত্যাগ করুন",
            "Avoid adding raw salt over cooked food. Excess sodium increases blood pressure, strains kidneys, and causes fluid retention.",
            "পাতে কাঁচা লবণ খাওয়া বন্ধ করুন। অতিরিক্ত সোডিয়াম রক্তচাপ বাড়ায়, হৃদরোগের ঝুঁকি তৈরি করে এবং কিডনির ওপর অতিরিক্ত চাপ সৃষ্টি করে।",
            "Heart & Pressure", "হৃদস্বাস্থ্য ও প্রেশার", "Favorite"
        ),
        DailyTip(
            5,
            "Incorporate Color in Meals",
            "খাদ্যতালিকায় রঙিন শাকসবজি রাখুন",
            "Eat colorful vegetables like carrots, spinach, tomatoes, and beetroot. Different colors indicate diverse antioxidants and vitamins.",
            "প্রতিদিনের খাবারে সবুজ, লাল, হলুদ রঙের শাকসবজি রাখুন। বিভিন্ন রঙের ফলে পাওয়া যায় নানা ধরনের অ্যান্টিঅক্সিডেন্ট ও প্রয়োজনীয় ভিটামিন।",
            "Nutrition & Diet", "পুষ্টি ও খাদ্যভ্যাস", "Restaurant"
        ),
        DailyTip(
            6,
            "Prioritize 7-8 Hours of Sleep",
            "প্রতি রাতে ৭-৮ ঘণ্টা গভীর ঘুম",
            "Consistent full sleep repairs muscle tissue, boosts brain memory consolidation, and stabilizes immune system response.",
            "প্রতি রাতে নিয়ম মেনে ৭-৮ ঘণ্টা ঘুমালে মস্তিষ্কের কার্যক্ষমতা বাড়ে, স্মৃতিশক্তি ভালো থাকে এবং অনাক্রম্যতা শক্তি শক্তিশালী হয়।",
            "Sleep & Rest", "ঘুম ও বিশ্রাম", "Bedtime"
        ),
        DailyTip(
            7,
            "Deep Breathing for Stress Relief",
            "মানসিক চাপ কমাতে গভীর শ্বাস-প্রশ্বাস",
            "Practice 4-7-8 breathing: Inhale for 4s, hold for 7s, exhale for 8s. This instantly calms your nervous system during anxiety.",
            "দুশ্চিন্তা দূর করতে ৪ সেকেন্ড শ্বাস নিন, ৭ সেকেন্ড আটকে রাখুন এবং ৮ সেকেন্ডে ধীরে ধীরে ছাড়ুন। এতে স্নায়ুতন্ত্র শান্ত হয়।",
            "Mental Health", "মানসিক স্বাস্থ্য", "Psychology"
        ),
        DailyTip(
            8,
            "Chew Your Food Thoroughly",
            "খাবার ভালোভাবে চিবিয়ে খান",
            "Chewing food 20-30 times per bite breaks down nutrients, mixes salivary enzymes, and prevents gastric bloating and acidity.",
            "প্রতি গ্রাস খাবার কমপক্ষে ২০-৩০ বার চিবিয়ে খেলে লালার এনজাইম খাবারের সাথে ভালোভাবে মেসে এবং বদহজম ও গ্যাসের সমস্যা দূর হয়।",
            "Digestive Health", "হজম ও পরিপাকতন্ত্র", "Restaurant"
        ),
        DailyTip(
            9,
            "Soak Up Morning Sunlight",
            "সকালের মিষ্টি রোদ গায়ে লাগান",
            "Spend 15 minutes in morning sunlight (before 10 AM) to boost Vitamin D synthesis, bone density, and natural circadian rhythm.",
            "সকাল ১০টার আগে ১৫ মিনিট রোদে থাকুন। এতে শরীরে প্রাকৃতিক ভিটামিন ডি তৈরি হয়, হাড় মজবুত হয় এবং মেজাজ ফুরফুরে থাকে।",
            "Immunity & Wellness", "রোগ প্রতিরোধ ক্ষমতা", "WbSunny"
        ),
        DailyTip(
            10,
            "Limit Screen Time Before Bed",
            "ঘুমানোর ১ ঘণ্টা আগে স্ক্রিন অফ করুন",
            "Blue light from smartphones inhibits melatonin secretion. Turn off devices 1 hour before sleeping for higher sleep quality.",
            "মোবাইল বা টিভির ব্লু-লাইট মেলাটোনিন হরমোন উৎপাদনে বাধা দেয়। ঘুমানোর ১ ঘণ্টা আগে ফোন দূরে রাখলে ঘুম দ্রুত ও গভীর হয়।",
            "Sleep & Rest", "ঘুম ও বিশ্রাম", "Smartphone"
        )
    )

    // Topics list to build 1000 structured tips programmatically with specific details
    private val tipTopics = listOf(
        Triple("Hydration & Hydration Pace", "পানির সঠিক পরিমাণ ও সময়", "Hydration & Water"),
        Triple("Green Tea & Antioxidants", "গ্রিন টি ও অ্যান্টিঅক্সিডেন্ট", "Nutrition & Diet"),
        Triple("Postural Health at Desk", "চেয়ারে বসার সঠিক ভঙ্গি", "Fitness & Movement"),
        Triple("Probiotics & Yogurt", "প্রোবায়োটিক ও দইয়ের পুষ্টি", "Digestive Health"),
        Triple("Calcium & Bone Density", "ক্যালসিয়াম ও হাড়ের যত্ন", "Senior Vitality"),
        Triple("Vitamin C & Skin Immunity", "ভিটামিন সি ও ত্বকের ইমিউনিটি", "Immunity & Wellness"),
        Triple("Mindful Eating Habits", "মাইন্ডফুল ইটিং ও সচেতন খাদ্য গ্রহণ", "Mental Health"),
        Triple("Flossing & Dental Hygiene", "ফ্লসিং ও মুখের স্বাস্থ্যবিধি", "Eye & Dental Care"),
        Triple("Sunscreen & UV Protection", "সানস্ক্রিন ও ত্বক সুরক্ষা", "Skin & Hygiene"),
        Triple("Omega-3 & Brain Power", "ওমেগা-৩ ও মেধার বিকাশ", "Heart & Pressure"),
        Triple("Iron Intake & Anemia", "আয়রন ও রক্তস্বল্পতা প্রতিরোধ", "Women & Child Care"),
        Triple("Stretching After Wakeup", "ঘুম থেকে উঠে হালকা স্ট্রেচিং", "Fitness & Movement"),
        Triple("Magnesium & Muscle Recovery", "ম্যাগনেসিয়াম ও পেশির শিথিলতা", "Nutrition & Diet"),
        Triple("Garlic for Immunity", "রসুন ও প্রাকৃতির অ্যান্টিবায়োটিক", "Immunity & Wellness"),
        Triple("Lemon Water Benefits", "লেবু পানির উপকারিতা", "Hydration & Water"),
        Triple("Walking 8,000 Steps Daily", "দৈনিক ৮,০০০ কদম হাঁটা", "Fitness & Movement"),
        Triple("Healthy Snacking with Nuts", "বাদাম ও হেলদি স্ন্যাকিং", "Nutrition & Diet"),
        Triple("Lowering Sugar Drinks", "মিষ্টি পানীয় পরিহারের উপায়", "Heart & Pressure"),
        Triple("Digital Detox Breaks", "ডিজিটাল ডিটক্স ও মানসিক প্রশান্তি", "Mental Health"),
        Triple("Warm Bath for Relaxation", "কুসুম গরম পানিতে গোসল", "Sleep & Rest"),
        Triple("Ginger Tea for Digestion", "আদা চা ও গ্যাস্ট্রিক প্রশমন", "Digestive Health"),
        Triple("Posture Alignment Exercises", "মেরুদণ্ড সোজা রাখার ব্যায়াম", "Fitness & Movement"),
        Triple("Hand Washing Hygiene", "সঠিক নিয়মে হাত ধোয়া", "Skin & Hygiene"),
        Triple("Fiber-Rich Oatmeal Breakfast", "ওটস ও ফাইবার সমৃদ্ধ প্রাতরাশ", "Nutrition & Diet"),
        Triple("Blood Pressure Self-Monitoring", "বাড়িতে প্রেশার মাপার নিয়ম", "Heart & Pressure"),
        Triple("Hydrating Skin in Winter", "শীতকালে ত্বকের বিশেষ যত্ন", "Skin & Hygiene"),
        Triple("Childhood Physical Activity", "শিশুদের খেলাধুলা ও বিকাশ", "Women & Child Care"),
        Triple("Senior Balance & Stability", "বয়স্কদের ভারসাম্য রক্ষার কৌশল", "Senior Vitality"),
        Triple("Preventing Acidity Naturally", "অ্যাসিডিটি প্রতিরোধের উপায়", "Digestive Health"),
        Triple("Eye Moisturization Tips", "চোখ ভেজা ও সুস্থ রাখার কৌশল", "Eye & Dental Care"),
        Triple("Chia Seeds & Fiber", "চিয়া সিড ও ওজন নিয়ন্ত্রণ", "Nutrition & Diet"),
        Triple("Avoiding Late Night Snacks", "রাতে দেরিতে খাওয়া এড়িয়ে চলা", "Sleep & Rest"),
        Triple("Dark Chocolate Benefits", "ডার্ক চকোলেট ও হার্টের সুরক্ষা", "Heart & Pressure"),
        Triple("Active Commute & Stairs", "লিফটের বদলে সিঁড়ি ব্যবহার", "Fitness & Movement"),
        Triple("Gratitude Journaling", "কৃতজ্ঞতা প্রকাশ ও মানসিক জোর", "Mental Health"),
        Triple("Honey & Warm Water Remedy", "মধু ও কুসুম গরম পানি", "Immunity & Wellness"),
        Triple("Managing Chronic Inflammation", "ইনফ্লেমেশন দূর করার খাবার", "Nutrition & Diet"),
        Triple("Proper Footwear for Walking", "হাঁটার জন্য সঠিক জুতা নির্বাচন", "Senior Vitality"),
        Triple("Caffeine Cutoff Time", "বিকেলের পর ক্যাফেইন পরিহার", "Sleep & Rest"),
        Triple("Nasal Saline Wash", "সাইナス ও নাকের স্বাস্থ্যবিধি", "Immunity & Wellness")
    )

    private val adviceTemplatesEn = listOf(
        "Make it a habit to practice %s consistently. Small daily changes build long-term metabolic health and resilience.",
        "To optimize your daily wellness, focus on %s. Medical experts recommend incorporating this into your core routine.",
        "Prioritizing %s directly improves cellular regeneration, lowers inflammation, and enhances overall bodily energy.",
        "Integrating %s into your schedule leads to noticeable improvements in mood, stamina, and biological longevity.",
        "Scientific research shows that %s reduces the risk of chronic conditions and supports sustainable organ health.",
        "Embrace %s as a foundation for daily vitality. Your body adapts rapidly when given consistency and care."
    )

    private val adviceTemplatesBn = listOf(
        "%s নিয়মিত মেনে চলুন। প্রতিদিনের এই ছোট পরিবর্তন দীর্ঘমেয়াদে আপনার রোগ প্রতিরোধ ক্ষমতা ও শারীরিক সুস্থতা গড়ে তুলবে।",
        "আপনার দৈনিক সুস্থতা নিশ্চিত করতে %s-এর ওপর গুরুত্ব দিন। স্বাস্থ্য বিশেষজ্ঞদের মতে এটি নিয়মিত করা অত্যন্ত জরুরি।",
        "%s নিয়মিত করলে কোষের পুনর্গঠন ত্বরান্বিত হয়, শরীরের প্রদাহ কমে এবং সারাদিনের কর্মোদ্দীপনা বৃদ্ধি পায়।",
        "আপনার অভ্যাসে %s যোগ করলে মানসিক প্রফুল্লতা, শক্তি এবং দীর্ঘমেয়াদী শারীরিক স্থায়িত্ব উল্লেখযোগ্যভাবে বৃদ্ধি পাবে।",
        "গবেষণায় দেখা গেছে যে %s মেনে চললে দীর্ঘমেয়াদী জটিল রোগের ঝুঁকি কমে এবং শরীরের গুরুত্বপূর্ণ অঙ্গসমূহ সুরক্ষিত থাকে।",
        "দৈনন্দিন জীবনীশক্তির ভিত্তি হিসেবে %s-কে গ্রহণ করুন। সঠিক যত্ন ও ধারাবাহিকতায় শরীর দ্রুত ইতিবাচক প্রতিক্রিয়া দেখায়।"
    )

    private val all1000Tips: List<DailyTip> by lazy {
        val list = mutableListOf<DailyTip>()
        list.addAll(curatedBaseTips)

        var idCounter = curatedBaseTips.size + 1

        while (list.size < 1000) {
            val topicIndex = (idCounter - 1) % tipTopics.size
            val templateIndex = (idCounter - 1) % adviceTemplatesEn.size
            val topic = tipTopics[topicIndex]

            val catPair = categoryMappings.firstOrNull { it.first == topic.third } 
                ?: ("General Wellness" to "সাধারণ সুস্বাস্থ্য")

            val iconName = when (topic.third) {
                "Nutrition & Diet" -> "Restaurant"
                "Hydration & Water" -> "WaterDrop"
                "Fitness & Movement" -> "DirectionsRun"
                "Mental Health" -> "Psychology"
                "Sleep & Rest" -> "Bedtime"
                "Heart & Pressure" -> "Favorite"
                "Immunity & Wellness" -> "Shield"
                "Digestive Health" -> "LocalPharmacy"
                "Skin & Hygiene" -> "Spa"
                "Eye & Dental Care" -> "Visibility"
                "Women & Child Care" -> "Face"
                else -> "Lightbulb"
            }

            val cycleNum = (idCounter / tipTopics.size) + 1
            val prefixEn = if (cycleNum > 1) "Pro Tip #$idCounter: " else ""
            val prefixBn = if (cycleNum > 1) "বিশেষ টিপস #$idCounter: " else ""

            val titleEn = "$prefixEn${topic.first}"
            val titleBn = "$prefixBn${topic.second}"

            val bodyEn = String.format(adviceTemplatesEn[templateIndex], topic.first.lowercase())
            val bodyBn = String.format(adviceTemplatesBn[templateIndex], topic.second)

            list.add(
                DailyTip(
                    id = idCounter,
                    titleEn = titleEn,
                    titleBn = titleBn,
                    bodyEn = bodyEn,
                    bodyBn = bodyBn,
                    categoryEn = catPair.first,
                    categoryBn = catPair.second,
                    iconName = iconName
                )
            )
            idCounter++
        }

        list
    }

    fun getAllTips(): List<DailyTip> = all1000Tips

    fun getTipById(id: Int): DailyTip? = all1000Tips.firstOrNull { it.id == id }

    fun getTipOfTheDay(): DailyTip {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val index = (dayOfYear * 7) % all1000Tips.size
        return all1000Tips[index]
    }

    fun getCategoriesEn(): List<String> {
        return listOf("All") + categoryMappings.map { it.first }
    }

    fun getCategoriesBn(): List<String> {
        return listOf("সব") + categoryMappings.map { it.second }
    }
}
