package com.srcreation.smarthelp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.util.Locale

class MedicalEncyclopediaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                EncyclopediaScreen(onBack = { finish() })
            }
        }
    }
}

data class IllnessData(
    val nameEn: String,
    val nameBn: String,
    val descEn: String,
    val descBn: String,
    val symptomsEn: String,
    val symptomsBn: String,
    val causesEn: String,
    val causesBn: String,
    val careEn: String,
    val careBn: String,
    val categoryEn: String,
    val categoryBn: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncyclopediaScreen(onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    var searchQuery by remember { mutableStateOf("") }
    val isBn = remember { Locale.getDefault().language == "bn" }

    val illnesses = remember {
        listOf(
            // 1. Cardiovascular
            IllnessData(
                "Hypertension (High Blood Pressure)", "উচ্চ রক্তচাপ (Hypertension)",
                "Persistently elevated blood pressure in systemic arteries.",
                "ধমনীর দেয়ালে রক্তের চাপ দীর্ঘমেয়াদে স্বাভাবিকের চেয়ে বেশি থাকা।",
                "Headache, dizziness, neck pain, chest palpitations, blurred vision.",
                "মাথাব্যথা, ঘাড় ব্যথা, বুক ধড়ফড় করা, মাথা ঘোরা, ঝাপসা দেখা।",
                "High salt intake, obesity, stress, genetics, lack of exercise.",
                "অতিরিক্ত লবণ খাওয়া, স্থূলতা, মানসিক চাপ, কায়িক শ্রমের অভাব, বংশগত কারণ।",
                "Low sodium diet, regular exercise, weight management, taking prescribed antihypertensives.",
                "লবণ কম খাওয়া, প্রতিদিন ৩০ মিনিট হাঁটা, ওজন কমানো ও নিয়মিত ডাক্তারের ওষুধ খাওয়া।",
                "Cardiovascular", "কার্ডিওভাসকুলার (হৃদরোগ)"
            ),
            IllnessData(
                "Myocardial Infarction (Heart Attack)", "হার্ট অ্যাটাক (Heart Attack)",
                "Ischemic necrosis of heart muscle due to blocked coronary artery.",
                "করোনারি ধমনী রক্তে ব্লক হয়ে হৃদপেশীর টিস্যু ক্ষয় পাওয়া।",
                "Crushing retrosternal chest pain, radiating to left arm/jaw, sweating, dyspnea.",
                "বুকের মাঝে তীব্র ভারী চাপ বা ব্যথা, যা বাম হাত বা চোয়ালে ছড়ায়, অতিরিক্ত ঘাম, শ্বাসকষ্ট।",
                "Atherosclerosis, coronary plaque, smoking, high cholesterol, uncontrolled diabetes.",
                "রক্তনালীতে চর্বি জমা, ধূমপান, অনিয়ন্ত্রিত ডায়াবেটিস ও উচ্চ কোলেস্টেরল।",
                "Call emergency CPR/ambulance immediately. Aspirin/Clopidogrel, oxygen, urgent CAG/stenting.",
                "তাৎক্ষণিক জরুরি অ্যাম্বুলেন্স ডাকুন। অ্যাসপিরিন সেবন, অক্সিজেন সাপোর্ট ও জরুরি হাসপাতালে ভর্তি।",
                "Cardiovascular", "কার্ডিওভাসকুলার (হৃদরোগ)"
            ),
            IllnessData(
                "Stroke / CVA (Cerebrovascular Accident)", "স্ট্রোক (Cerebrovascular Accident)",
                "Sudden neurological deficit from cerebral ischemia or hemorrhage.",
                "মস্তিষ্কের রক্তনালী ব্লক হয়ে বা ফেটে রক্ত সরবরাহ বন্ধ হওয়া।",
                "Face drooping, arm weakness, slurred speech, sudden confusion, loss of balance (FAST).",
                "মুখ বেঁকে যাওয়া, হাত বা পা অবশ হওয়া, কথা জড়িয়ে যাওয়া, হঠাৎ জ্ঞান হারানো (FAST)।",
                "Hypertension, arterial thrombus, atrial fibrillation, smoking.",
                "উচ্চ রক্তচাপ, ধমনীতে রক্ত জমাট বাঁধা, অনিয়মিত হৃদস্পন্দন, ধূমপান।",
                "Urgent CT Brain scan within golden hours (3-4.5 hrs). Thrombolysis / ICU care.",
                "গোল্ডেন আওয়ারের (৩-৪.৫ ঘণ্টা) মধ্যে জরুরি সিটি স্ক্যান ও আইসিইউ/স্ট্রোক কেয়ারে ভর্তি।",
                "Cardiovascular", "কার্ডিওভাসকুলার (হৃদরোগ)"
            ),
            IllnessData(
                "Heart Failure (Congestive Heart Failure)", "হার্ট ফেইলিওর (Heart Failure)",
                "Inability of the heart to pump blood efficiently to meet body requirements.",
                "হৃদপিণ্ডের রক্ত পাম্প করার ক্ষমতা কমে গিয়ে শরীরের চাহিদা মেটাতে ব্যর্থ হওয়া।",
                "Shortness of breath (dyspnea), swollen ankles/legs, fatigue, persistent cough.",
                "শ্বাসকষ্ট, পায়ের গোড়ালি বা পা ফুলে যাওয়া, ক্লান্তি, দীর্ঘস্থায়ী কাশি।",
                "Coronary artery disease, past heart attack, hypertension, cardiomyopathy.",
                "করোনারি ধমনী রোগ, পূর্বের হার্ট অ্যাটাক, উচ্চ রক্তচাপ, কার্ডিওমায়োপ্যাথি।",
                "Sodium and fluid restriction, Diuretics (Furosemide), ACE inhibitors, regular monitoring.",
                "লবণ ও পানি সীমিত রাখা, ডাইউরেটিক্স (ল্যাসিক্স) সেবন এবং নিয়মিত চিকিৎসকের ফলোআপ।",
                "Cardiovascular", "কার্ডিওভাসকুলার (হৃদরোগ)"
            ),
            IllnessData(
                "Atrial Fibrillation (AFib)", "এট্রিয়াল ফিব্রিলেশন (अनિયমিত হৃদস্পন্দন)",
                "Irregular and often rapid heart rate causing poor blood flow.",
                "হৃদপিণ্ডের ওপরের প্রকোষ্ঠের অনিয়মিত ও দ্রুত স্পন্দন যা রক্ত চলাচলে ব্যাঘাত ঘটায়।",
                "Palpitations, fatigue, dizziness, shortness of breath, chest discomfort.",
                "বুক ধড়ফড় করা, ক্লান্তি, মাথা ঘোরা, শ্বাসকষ্ট, বুকে অস্বস্তি।",
                "High blood pressure, heart valve disease, coronary artery disease, thyroid issues.",
                "উচ্চ রক্তচাপ, ভালভজনিত সমস্যা, করোনারি হৃদরোগ, থাইরয়েডের সমস্যা।",
                "Rate-control medications, anticoagulants (blood thinners) to prevent stroke, cardioversion.",
                "হৃদস্পন্দন নিয়ন্ত্রণের ওষুধ, স্ট্রোক প্রতিরোধে রক্ত পাতলা করার ওষুধ ও কার্ডিওভার্শন।",
                "Cardiovascular", "কার্ডিওভাসকুলার (হৃদরোগ)"
            ),

            // 2. Respiratory
            IllnessData(
                "Bronchial Asthma", "ব্রোঞ্চিয়াল হাঁপানি (Asthma)",
                "Chronic inflammatory airway disease causing bronchospasm and wheezing.",
                "শ্বাসনালীর দীর্ঘমেয়াদী প্রদাহ যার ফলে বাতাস চলাচলে বাধা সৃষ্টি হয়।",
                "Wheezing, shortness of breath, chest tightness, nocturnal coughing.",
                "সাঁ সাঁ শব্দ, শ্বাসকষ্ট, বুকে চাপ ধরা ভাব, রাতে কাশি বৃদ্ধি।",
                "Allergens (dust, pollen), cold air, exercise, respiratory infections, smoking.",
                "অ্যালার্জেন (ধূলোবালি, পরাগরেণু), ঠান্ডা বাতাস, ব্যায়াম, শ্বাসনালীর ইনফেকশন, ধূমপান।",
                "Avoid triggers, use inhalers (bronchodilators and corticosteroids) as prescribed.",
                "অ্যালার্জি সৃষ্টিকারী উপাদান বর্জন, ডাক্তারের পরামর্শ অনুযায়ী ইনহেলার ব্যবহার।",
                "Respiratory", "শ্বসনতন্ত্র ও ফুসফুস"
            ),
            IllnessData(
                "Chronic Obstructive Pulmonary Disease (COPD)", "সিওপিডি / দীর্ঘস্থায়ী ফুসফুস রোগ (COPD)",
                "Progressive inflammatory lung disease causing obstructed airflow from lungs.",
                "ধূমপান ও দূষণের কারণে ফুসফুসের বাতাস চলাচলের রাস্তা সংকুচিত ও ক্ষতিগ্রস্ত হওয়া।",
                "Chronic productive cough, shortness of breath during exertion, wheezing, fatigue.",
                "দীর্ঘমেয়াদী কফযুক্ত কাশি, সামান্য পরিশ্রমে শ্বাসকষ্ট, সাঁ সাঁ আওয়াজ, ক্লান্তি।",
                "Long-term smoking, biomass fuel smoke, air pollution, genetic alpha-1 deficiency.",
                "দীর্ঘদিন ধূমপান করা, কাঠের ধোঁয়া, বায়ু দূষণ, জিনগত কারণ।",
                "Quit smoking completely, use bronchodilator inhalers, pulmonary rehabilitation, oxygen therapy.",
                "ধূমপান সম্পূর্ণ ত্যাগ, ব্রোঙ্কোডাইলেটর ইনহেলার ব্যবহার এবং অক্সিজেন থেরাপি।",
                "Respiratory", "শ্বসনতন্ত্র ও ফুসফুস"
            ),
            IllnessData(
                "Pneumonia", "নিউমোনিয়া (Pneumonia)",
                "Infection that inflames air sacs in one or both lungs, filling them with fluid.",
                "এক বা উভয় ফুসফুসের অ্যালভিওলাইতে ইনফেকশন হয়ে পুঁজ বা তরল জমা হওয়া।",
                "Cough with phlegm/pus, fever, chills, sharp chest pain when breathing or coughing.",
                "কফ বা পুঁজযুক্ত কাশি, জ্বর, কাঁপুনি, শ্বাস নেওয়ার সময় বুকে তীব্র ব্যথা।",
                "Bacterial (Streptococcus pneumoniae), viral, or fungal infections.",
                "ব্যাকটেরিয়া, ভাইরাস বা ছত্রাকের আক্রমণ।",
                "Antibiotics (if bacterial), rest, hydration, antipyretics, and oxygen support if severe.",
                "ডাক্তারের পরামর্শে অ্যান্টিবায়োটিক, পর্যাপ্ত বিশ্রাম, তরল খাবার ও প্রয়োজনে অক্সিজেন।",
                "Respiratory", "শ্বসনতন্ত্র ও ফুসফুস"
            ),
            IllnessData(
                "Pulmonary Embolism", "পালমোনারি এমবলিজম (ফুসফুসে রক্ত জমাট)",
                "Blockage in one of the pulmonary arteries in the lungs, usually from leg clots.",
                "সাধারণত পায়ের গভীর শিরা থেকে রক্ত জমাট বেঁধে ফুসফুসের ধমনীতে আটকে যাওয়া।",
                "Sudden shortness of breath, chest pain, rapid heart rate, coughing up blood.",
                "হঠাৎ তীব্র শ্বাসকষ্ট, বুকে ব্যথা, দ্রুত হৃদস্পন্দন, কাশির সাথে রক্ত যাওয়া।",
                "Deep vein thrombosis (DVT), prolonged immobility, surgery, clotting disorders.",
                "পায়ের গভীর শিরার রক্ত জমাট বাঁধা (DVT), দীর্ঘক্ষণ বিছানায় শুয়ে থাকা, সার্জারি।",
                "Emergency medical care, anticoagulant therapy (blood thinners), oxygen, sometimes thrombolysis.",
                "জরুরি হাসপাতালে ভর্তি, রক্ত পাতলা করার ওষুধ (অ্যান্টিসোকোয়গুলেন্ট) ও অক্সিজেন থেরাপি।",
                "Respiratory", "শ্বসনতন্ত্র ও ফুসফুস"
            ),

            // 3. Gastrointestinal & Liver
            IllnessData(
                "Peptic Ulcer Disease (PUD)", "পেপটিক আলসার / গ্যাস্ট্রিক (PUD)",
                "Sores that develop on the lining of the stomach, small intestine, or esophagus.",
                "পাকস্থলী বা ক্ষুদ্রান্ত্রের দেয়ালে এসিডের কারণে ক্ষত তৈরি হওয়া।",
                "Burning stomach pain, bloating, heartburn, nausea, intolerance to fatty foods.",
                "পেটে জ্বালাপোড়া বা ব্যথা, পেট ফাঁপা, বুক জ্বলা, বমি ভাব, তৈলাক্ত খাবারে অরুচি।",
                "H. pylori bacterial infection, regular use of NSAIDs (Ibuprofen, Diclofenac), stress.",
                "এইচ পাইলোরি ব্যাকটেরিয়া, নিয়মিত পেইনকিলার (আইবুপ্রোফেন/ডাইক্লোফেনাক) সেবন, মানসিক চাপ।",
                "PPIs (Omeprazole/Pantoprazole), antibiotics for H. pylori, avoid spicy/fatty foods.",
                "পিপিআই (ওমেপ্রাজল/প্যান্টোপ্রাজল) সেবন, ঝাল-তেলযুক্ত খাবার বর্জন ও নিয়ম মেনে খাওয়া।",
                "Gastrointestinal", "পরিপাকতন্ত্র ও যকৃত"
            ),
            IllnessData(
                "Liver Cirrhosis", "লিভার সিরোসিস (Liver Cirrhosis)",
                "Late stage of scarring of the liver caused by many forms of liver diseases.",
                "দীর্ঘমেয়াদী লিভারের ক্ষতির কারণে লিভার স্ক্যারিং বা শক্ত হয়ে অকার্যকর হয়ে পড়া।",
                "Fatigue, easily bleeding/bruising, jaundice, ascites (abdominal swelling), confusion.",
                "ক্লান্তি, সহজেই রক্তপাত বা কালচে দাগ, জন্ডিস, পেটে পানি জমা (Ascites), মানসিক বিভ্রান্তি।",
                "Chronic alcoholism, Hepatitis B & C infections, non-alcoholic fatty liver disease (NAFLD).",
                "অতিরিক্ত অ্যালকোহল সেবন, হেপাটাইটিস বি ও সি ভাইরাস, ফ্যাটি লিভার ডিজিজ।",
                "Stop alcohol, treat underlying hepatitis, low sodium diet, regular hepatology follow-up.",
                "মদ সম্পূর্ণ বর্জন, হেপাটাইটিস চিকিৎসা, কম লবণযুক্ত খাবার এবং নিয়মিত লিভার বিশেষজ্ঞের পরামর্শ।",
                "Gastrointestinal", "পরিপাকতন্ত্র ও যকৃত"
            ),
            IllnessData(
                "Acute Appendicitis", "অ্যাপেন্ডিসাইটিস (Appendicitis)",
                "Inflammation of the appendix, causing acute lower right abdominal pain.",
                "পেটের ডান দিকের নিচের অংশে অবস্থিত অ্যাপেন্ডিক্সের তীব্র প্রদাহ বা ইনফেকশন।",
                "Pain starting around navel shifting to lower right abdomen, nausea, vomiting, low fever.",
                "নাভির চারপাশ থেকে শুরু হয়ে পেটের ডানপাশে তীব্র ব্যথা, বমি ভাব, জ্বর।",
                "Blockage of the appendiceal lumen by stool, foreign body, or lymphoid hyperplasia.",
                "মল বা অন্য কোনো কারণে অ্যাপেন্ডিক্সের মুখ বন্ধ হয়ে যাওয়া।",
                "Surgical emergency! Urgent Appendectomy (surgical removal) to prevent rupture.",
                "জরুরি সার্জিক্যাল ইমার্জেন্সি! অ্যাপেন্ডিক্স ফেটে যাওয়ার আগেই দ্রুত অপারেশন (Appendectomy) করা।",
                "Gastrointestinal", "পরিপাকতন্ত্র ও যকৃত"
            ),
            IllnessData(
                "Gastroenteritis (Stomach Flu)", "গ্যাস্ট্রোএন্টেরাইটিস / পাতলা পায়খানা",
                "Inflammation of the stomach and intestines, typically resulting from bacterial/viral infection.",
                "ভাইরাস বা ব্যাকটেরিয়ার আক্রমণে পাকস্থলী ও অন্ত্রের প্রদাহ ও ইনফেকশন।",
                "Watery diarrhea, abdominal cramps, nausea, vomiting, low-grade fever, dehydration.",
                "পানি পায়খানা, পেটে মোচড় দিয়ে ব্যথা, বমি ভাব, বমি, হালকা জ্বর, পানিশূন্যতা।",
                "Rotavirus, Norovirus, contaminated food/water (food poisoning).",
                "রোটাভাইরাস, নোরোভাইরাস, দূষিত খাবার বা পানি (খাদ্যে বিষক্রিয়া)।",
                "Oral rehydration solution (ORS), zinc supplements, bland diet, probiotics. Antibiotics if bacterial.",
                "প্রচুর ওআরএস স্যালাইন, জিংক ট্যাবলেট, হালকা খাবার এবং চিকিৎসকের পরামর্শে অ্যান্টিবায়োটিক।",
                "Gastrointestinal", "পরিপাকতন্ত্র ও যকৃত"
            ),

            // 4. Infectious Diseases
            IllnessData(
                "Malaria", "ম্যালেরিয়া জ্বর (Malaria)",
                "Mosquito-borne infectious disease caused by Plasmodium parasites.",
                "অ্যানোফিলিস মশার কামড়ে প্লাজমোডিয়াম পরজীবীর মাধ্যমে ছড়ানো জ্বর।",
                "Cycles of chills, high fever, profuse sweating, headache, fatigue, anemia.",
                "কাঁপুনি দিয়ে তীব্র জ্বর, প্রচুর ঘাম হওয়া, মাথাব্যথা, ক্লান্তি, রক্তাল্পতা।",
                "Bite of infected female Anopheles mosquito transmitting Plasmodium.",
                "সংক্রমিত স্ত্রী অ্যানোফিলিস মশার কামড়।",
                "Antimalarial medications (ACTs, Chloroquine), mosquito nets, preventive measures.",
                "ডাক্তারের পরামর্শে ম্যালেরিয়ার ওষুধ সেবন, মশারি ব্যবহার ও মশার বংশবিস্তার রোধ।",
                "Infectious", "সংক্রামক ব্যাধি"
            ),
            IllnessData(
                "Tuberculosis (TB)", "যক্ষ্মা বা টিবি (Tuberculosis)",
                "Contagious bacterial infection caused by Mycobacterium tuberculosis affecting lungs.",
                "মাইকোব্যাকটেরিয়াম টিউবারকিউলোসিস ব্যাকটেরিয়ার কারণে ফুসফুস ও অন্যান্য অঙ্গের রোগ।",
                "Chronic cough lasting >3 weeks, coughing up blood, chest pain, night sweats, weight loss.",
                "৩ সপ্তাহের বেশি স্থায়ী কাশি, কাশির সাথে রক্ত যাওয়া, বুকে ব্যথা, রাতে ঘাম হওয়া, ওজন হ্রাস।",
                "Inhalation of airborne droplets from infected person's cough or sneeze.",
                "আক্রান্ত ব্যক্তির কাশি বা হাঁচির মাধ্যমে বাতাসে ছড়ানো জীবাণু শ্বাসের সাথে নেওয়া।",
                "6-9 months of DOTS antitubercular therapy (Rifampicin, Isoniazid, Pyrazinamide, Ethambutol).",
                "৬-৯ মাস নিয়মানুযায়ী ডটস (DOTS) পদ্ধতিতে যক্ষ্মার সম্পূর্ণ কোর্স ওষুধ সেবন।",
                "Infectious", "সংক্রামক ব্যাধি"
            ),
            IllnessData(
                "Viral Hepatitis (A, B, C)", "ভাইরাল হেপাটাইটিস / জন্ডিস (Hepatitis)",
                "Inflammatory condition of the liver caused by viral infections.",
                "ভাইরাসের সংক্রমণে যকৃতে প্রদাহ ও জন্ডিস সৃষ্টি হওয়া।",
                "Jaundice (yellow skin/eyes), dark urine, fatigue, abdominal pain, nausea.",
                "চোখ ও ত্বক হলুদ হওয়া (জন্ডিস), গাড় রঙের প্রস্রাব, ক্লান্তি, পেটে ব্যথা, বমি ভাব।",
                "Hepatitis A/E (contaminated food/water), Hepatitis B/C (blood, needles, sexual contact).",
                "হেপাটাইটিস এ/ই (দূষিত পানি ও খাবার), হেপাটাইটিস বি/সি (রক্ত ও সিরিঞ্জের মাধ্যমে)।",
                "Rest, high carbohydrate diet, avoid hepatotoxic drugs/alcohol, antiviral therapy for B/C.",
                "বিশ্রাম, শর্করাজাতীয় খাবার গ্রহণ, লিভারের ক্ষতি করে এমন ওষুধ ও মদ বর্জন, এবং ডাক্তারের পরামর্শ।",
                "Infectious", "সংক্রামক ব্যাধি"
            ),
            IllnessData(
                "COVID-19", "কোভিড-১৯ / করোনা ভাইরাস",
                "Respiratory illness caused by SARS-CoV-2 coronavirus.",
                "সার্স-কোভ-২ ভাইরাসের কারণে শ্বসনতন্ত্রের সংক্রমণজনিত রোগ।",
                "Fever, dry cough, fatigue, loss of taste or smell, shortness of breath, body ache.",
                "জ্বর, শুষ্ক কাশি, ক্লান্তি, স্বাদ বা গন্ধ হারিয়ে যাওয়া, শ্বাসকষ্ট, গায়ে ব্যথা।",
                "Airborne transmission via respiratory droplets from infected individuals.",
                "আক্রান্ত ব্যক্তির শ্বাস-প্রশ্বাসের ড্রপলেট বা বায়ুবাহিত কণার মাধ্যমে ছড়ায়।",
                "Isolation, hydration, antipyretics, antiviral medications if high risk, oxygen support.",
                "আইসোলেশন, পর্যাপ্ত তরল ও পানি পান, প্যারাসিটামল এবং প্রয়োজনে অক্সিজেন থেরাপি।",
                "Infectious", "সংক্রামক ব্যাধি"
            ),

            // 5. Metabolic & Endocrine
            IllnessData(
                "Hyperthyroidism", "হাইপারথাইরয়েডিজম (Hyperthyroidism)",
                "Overactive thyroid gland producing excessive thyroid hormones.",
                "থাইরয়েড গ্রন্থি থেকে অতিরিক্ত T3 ও T4 হরমোন তৈরি হওয়া।",
                "Weight loss despite increased appetite, rapid heartbeat (tachycardia), anxiety, tremor.",
                "খিদে বেশি হওয়া সত্ত্বেও ওজন হ্রাস, দ্রুত হৃদস্পন্দন, অস্থিরতা, হাত কাঁপা।",
                "Grave's disease (autoimmune), thyroid nodules, excess iodine intake.",
                "গ্রেভস ডিজিজ (অটোহিমিউন রোগ), থাইরয়েড নোডিউল, অতিরিক্ত আয়োডিন।",
                "Antithyroid drugs (Carbimazole), radioactive iodine, or surgical thyroidectomy.",
                "অ্যান্টিথাইরয়েড ওষুধ (কার্বিমাজোল), রেডিওঅ্যাক্টিভ আয়োডিন বা সার্জারি।",
                "Metabolic", "মেটাবলিক ও হরমোন"
            ),
            IllnessData(
                "Gout (Gouty Arthritis)", "গাউট বা গেঁটেবাত (Gout)",
                "Form of inflammatory arthritis characterized by recurrent attacks of severe pain in joints.",
                "রক্তে ইউরিক এসিড বেড়ে গিয়ে জোড়ায় ক্রিস্টাল জমা হয়ে তীব্র প্রদাহ ও ব্যথা হওয়া।",
                "Intense joint pain (especially big toe), swelling, redness, warmness in joints.",
                "জোড়ায় তীব্র ব্যথা (বিশেষ করে পায়ের বুড়ো আঙুল), ফোলা, লাল হয়ে যাওয়া, গরম অনুভূত হওয়া।",
                "High uric acid (hyperuricemia) from diet (red meat, seafood) or poor kidney excretion.",
                "উচ্চ ইউরিক এসিড (লাল মাংস, সামুদ্রিক মাছ খাওয়া) বা কিডনি কর্তৃক কম ইউরিক এসিড নিষ্কাশন।",
                "Allopurinol, Colchicine, NSAIDs for acute pain, low purine diet, plenty of water.",
                "অ্যালোপ्यूरিনোল, কোলচিসিন, পেইনকিলার, পিউরিন সমৃদ্ধ খাবার বর্জন ও প্রচুর পানি পান।",
                "Metabolic", "মেটাবলিক ও হরমোন"
            ),

            // 6. Neurology & Mental Health
            IllnessData(
                "Migraine", "মাইলগ্রেন / মাইগ্রেন মাথাব্যথা",
                "Neurological condition causing severe throbbing headaches, usually on one side.",
                "মস্তিষ্কের রক্তনালীর অস্বাভাবিক প্রসারণের কারণে সাধারণত মাথার একপাশে তীব্র দপদপে ব্যথা।",
                "Throbbing headache, nausea, vomiting, extreme sensitivity to light and sound (aura).",
                "মাথার একপাশে দপদপে ব্যথা, বমি ভাব, আলো ও শব্দে তীব্র অস্বস্তি (অরা)।",
                "Triggers like stress, lack of sleep, certain foods, hormonal changes, bright lights.",
                "মানসিক চাপ, ঘুমের অনিয়ম, নির্দিষ্ট কিছু খাবার, হরমোনের পরিবর্তন, অতিরিক্ত আলো।",
                "Triptans, pain relievers, avoiding specific triggers, adequate sleep and stress management.",
                "ট্রিপটান জাতীয় ওষুধ, পেইনকিলার, ট্রিগার খাবার বর্জন, পর্যাপ্ত ঘুম ও চাপমুক্ত থাকা।",
                "Neurology", "মস্তিষ্ক ও স্নায়ু রোগ"
            ),
            IllnessData(
                "Epilepsy (Seizure Disorder)", "খিঁচুনি বা মৃগী রোগ (Epilepsy)",
                "Central nervous system disorder in which brain activity becomes abnormal.",
                "মস্তিষ্কের বৈদ্যুতিক কার্যক্রমে আকস্মিক অস্বাভাবিকতার ফলে বারবার খিঁচুনি হওয়া।",
                "Temporary confusion, staring spells, uncontrollable jerking movements of arms/legs.",
                "ক্ষণস্থায়ী বিভ্রান্তি, শূন্যদৃষ্টিতে তাকিয়ে থাকা, হাত-পায়ের অবাধ্য খিঁচুনি বা কাঁপুনি।",
                "Genetics, head trauma, brain infections (meningitis), stroke, brain tumors.",
                "বংশগত কারণ, মাথায় আঘাত, মস্তিষ্কের ইনফেকশন (মেনিনজাইটিস), স্ট্রোক, টিউমার।",
                "Antiepileptic drugs (Sodium Valproate, Levetiracetam), avoid driving/swimming during episodes.",
                "নিয়মিত মৃগী রোগের ওষুধ (ভালপ্রোয়েট/লেভেটিরাসেটাম) সেবন এবং ডাক্তারের পরামর্শ মেনে চলা।",
                "Neurology", "মস্তিষ্ক ও স্নায়ু রোগ"
            ),
            IllnessData(
                "Major Depressive Disorder (Depression)", "মেজর ডিপ্রেশন / বিষণ্ণতা",
                "Common and serious medical illness that negatively affects how you feel and think.",
                "দীর্ঘস্থায়ী মন খারাপ, হতাশা এবং জীবনের প্রতি আগ্রহ হারিয়ে ফেলা মানসিক রোগ।",
                "Persistent sadness, loss of interest in activities, fatigue, sleep disturbances, suicidal thoughts.",
                "দীর্ঘস্থায়ী মন খারাপ, কোনো কিছুতে আগ্রহ না পাওয়া, অতিরিক্ত ক্লান্তি, ঘুমের ব্যাঘাত, আত্মহত্যার চিন্তা।",
                "Brain chemistry imbalance, trauma, genetics, chronic illness, life stress.",
                "মস্তিষ্কের কেমিক্যালের ভারসাম্যহীনতা, মানসিক আঘাত, বংশগত কারণ, দীর্ঘস্থায়ী রোগ।",
                "Psychotherapy (CBT), antidepressants (SSRIs like Fluoxetine/Sertraline), social support.",
                "সাইকোথেরাপি, অ্যান্টিডিপ্রেসেন্ট ওষুধ (এসএসআরআই), পরিবার ও বন্ধুদের মানসিক সাপোর্ট।",
                "Mental Health", "মানসিক স্বাস্থ্য"
            ),
            IllnessData(
                "Generalized Anxiety Disorder (GAD)", "জেনারেলড অ্যানজাইটি ডিসঅর্ডার (উদ্বেগ)",
                "Characterized by persistent and excessive worry about various things.",
                "যেকোনো বিষয়ে অতিরিক্ত, নিয়ন্ত্রণহীন ও দীর্ঘস্থায়ী দুশ্চিন্তা ও ভয় কাজ করা।",
                "Restlessness, feeling on edge, fatigue, difficulty concentrating, muscle tension, insomnia.",
                "অস্থিরতা, সবসময় ভীতি কাজ করা, ক্লান্তি, মনোযোগে সমস্যা, মাংসপেশিতে টান, অনিদ্রা।",
                "Brain chemistry, genetics, stress, personality factors.",
                "মস্তিষ্কের কেমিক্যাল, জিনগত কারণ, অতিরিক্ত মানসিক চাপ।",
                "Cognitive Behavioral Therapy (CBT), anxiolytics, relaxation techniques, lifestyle changes.",
                "কগনিটিভ বিহেভিওরাল থেরাপি (CBT), উদ্বেগের ওষুধ, রিলাক্সেশন এক্সারসাইজ ও জীবনযাত্রার পরিবর্তন।",
                "Mental Health", "মানসিক স্বাস্থ্য"
            ),

            // 7. Nephrology & Urology
            IllnessData(
                "Chronic Kidney Disease (CKD)", "দীর্ঘস্থায়ী কিডনি রোগ (CKD)",
                "Gradual loss of kidney function over months or years.",
                "মাসে বা বছরে ধীরে ধীরে কিডনির রক্ত ফিল্টার করার ক্ষমতা স্থায়ীভাবে নষ্ট হওয়া।",
                "Swelling in feet/ankles (edema), fatigue, nausea, foamy urine, high blood pressure.",
                "পা ও গোড়ালি ফুলে যাওয়া (এডিমা), ক্লান্তি, বমি ভাব, ফেনা ফেনা প্রস্রাব, উচ্চ রক্তচাপ।",
                "Uncontrolled diabetes, long-standing hypertension, glomerulonephritis, kidney stones.",
                "অনিয়ন্ত্রিত ডায়াবেটিস, দীর্ঘদিনের উচ্চ রক্তচাপ, গ্লোমেরুলোনেফ্রাইটিস, কিডনি স্টোন।",
                "Strict blood sugar & BP control, low protein/salt diet, nephrology care, dialysis/transplant if end-stage.",
                "রক্তে শর্করা ও প্রেশার কঠোর নিয়ন্ত্রণে রাখা, কম প্রোটিন ও কম লবণযুক্ত খাবার এবং ডাক্তারের ফলোআপ।",
                "Nephrology", "কিডনি ও মূত্রনালী"
            ),
            IllnessData(
                "Kidney Stones (Nephrolithiasis)", "কিডনি স্টোন / পাথরী",
                "Hard deposits made of minerals and salts that form inside kidneys.",
                "কিডনিতে খনিজ ও লবণের ক্রিস্টাল জমে পাথর তৈরি হওয়া এবং মূত্রনালীতে আটকে যাওয়া।",
                "Severe sharp pain in back/side (renal colic), blood in urine, nausea, painful urination.",
                "পিঠে বা কোমরে তীব্র ব্যথা (রেনাল কলিক), প্রস্রাবের সাথে রক্ত যাওয়া, বমি ভাব, প্রস্রাবে জ্বালাপোড়া।",
                "Dehydration, high oxalate diet, calcium/uric acid imbalances, family history.",
                "অপর্যাপ্ত পানি পান, অক্সালেটযুক্ত খাবার, ক্যালসিয়াম ও ইউরিক এসিডের ভারসাম্যহীনতা।",
                "High fluid intake (3L+/day), pain management, lithotripsy (ESWL), or surgical removal.",
                "প্রচুর পানি পান (প্রতিদিন ৩ লিটার+), পেইনকিলার, লিথোট্রিপ্সি (শব্দতরঙ্গে পাথর ভাঙা) বা সার্জারি।",
                "Nephrology", "কিডনি ও মূত্রনালী"
            ),
            IllnessData(
                "Urinary Tract Infection (UTI)", "ইউটিআই / মূত্রনালীর ইনফেকশন",
                "Infection in any part of the urinary system (kidneys, ureters, bladder, urethra).",
                "মূত্রনালী, মূত্রথলি বা কিডনিতে ব্যাকটেরিয়ার আক্রমণ ও ইনফেকশন হওয়া।",
                "Burning sensation during urination, frequent urgent need to urinate, cloudy/strong-smelling urine.",
                "প্রস্রাবে তীব্র জ্বালাপোড়া, ঘনঘন প্রস্রাবের বেগ কিন্তু অল্প হওয়া, ঘোলা বা দুর্গন্ধযুক্ত প্রস্রাব।",
                "Bacterial infection (commonly E. coli entering urinary tract), poor hygiene, dehydration.",
                "ব্যাকটেরিয়ার সংক্রমণ (বিশেষ করে ই. কোলাই), অপর্যাপ্ত পানি পান, অস্বাস্থ্যকর অভ্যাস।",
                "Antibiotics (Nitrofurantoin/Ciprofloxacin), drink plenty of water, cranberry juice.",
                "ডাক্তারের পরামর্শে অ্যান্টিবায়োটিক (যেমন: নাইট্রোফিউরেন্টইন), প্রচুর পানি পান করা।",
                "Nephrology", "কিডনি ও মূত্রনালী"
            ),

            // 8. Dermatology & Immunology
            IllnessData(
                "Psoriasis", "সোরিয়াসিস (Psoriasis)",
                "Autoimmune disease that causes skin cells to build up rapidly, forming silvery scales.",
                "অটোহিমিউন রোগের কারণে ত্বকের কোষ দ্রুত বৃদ্ধি পেয়ে লালচে ও রূপালী আঁশযুক্ত চামড়া হওয়া।",
                "Red patches of skin covered with thick, silvery scales, dry cracked skin, itching/burning.",
                "ত্বকে লালচে ছোপ এবং ওপরের রূপালী রঙের পুরু চামড়া, শুষ্ক ত্বক, চুলকানি বা জ্বালাপোড়া।",
                "Immune system dysfunction attacking healthy skin cells, genetics, stress.",
                "রোগ প্রতিরোধ ক্ষমতার ত্রুটির কারণে ত্বকের ওপর আক্রমণ, জিনগত কারণ, মানসিক চাপ।",
                "Topical corticosteroids, Vitamin D analogues, phototherapy, systemic immunosuppressants.",
                "টপিক্যাল কর্টিকোসস্টেরয়েড ক্রিম, ভিটামিন ডি ক্রিম, ফটোথেরাপি এবং ডাক্তারের পরামর্শ অনুযায়ী ওষুধ।",
                "Dermatology", "ত্বক ও রোগ প্রতিরোধ"
            ),
            IllnessData(
                "Eczema (Atopic Dermatitis)", "একজিমা / অ্যাটোপিক ডার্মাটাইটিস",
                "Condition that makes your skin red and itchy, common in children but can occur at any age.",
                "ত্বকের এক ধরনের দীর্ঘমেয়াদী প্রদাহ যা তীব্র চুলকানি, লালচে ভাব ও শুষ্কতা তৈরি করে।",
                "Dry, itchy skin, red to brownish-gray patches, small raised bumps, cracked sensitive skin.",
                "ত্বকে প্রচণ্ড চুলকানি, লালচে-ধূসর ছোপ, ছোট ফুসকুড়ি, ত্বক ফেটে যাওয়া।",
                "Genetics, immune system activation, environmental triggers, dry skin.",
                "বংশগত কারণ, রোগ প্রতিরোধ ক্ষমতার অতিপ্রতিক্রিয়া, শুষ্ক ত্বক, পরিবেশগত ট্রিগার।",
                "Moisturizers (emollients), topical steroids, avoiding harsh soaps and allergens.",
                "নিয়মিত ময়েশ্চারাইজার ও ক্রিম ব্যবহার, হার্শ সাবান বা কেমিক্যাল বর্জন, স্টেরয়েড ক্রিম।",
                "Dermatology", "ত্বক ও রোগ প্রতিরোধ"
            ),
            IllnessData(
                "Urticaria (Hives)", "আমবাত / হাইভস (Urticaria)",
                "Outbreak of pale red, swollen wheals or plaques on skin that appear suddenly.",
                "ত্বকে হঠাৎ লালচে ও ফুলে ওঠা চাকা চাকা দাগ বা আমবাত হওয়া এবং তীব্র চুলকানি হওয়া।",
                "Itchy welts (wheals) that can appear anywhere on the body, swelling of lips/eyes (angioedema).",
                "শরীরে চুলকানিযুক্ত চাকা দাগ, ঠোঁট বা চোখ ফুলে যাওয়া (অ্যাঞ্জিওয়িডেমা)।",
                "Allergic reaction to food, medications, insect stings, infections, or physical stimuli.",
                "খাবার, ওষুধ, পোকামাকড়ের কামড়, ইনফেকশন বা তাপমাত্রার পরিবর্তনের কারণে অ্যালার্জি।",
                "Antihistamines (Cetirizine/Loratadine), avoid triggers, cool compresses.",
                "অ্যান্টিহিস্টামিন ওষুধ (সেটিরিজিন/লোরাটাডিন) সেবন, অ্যালার্জি সৃষ্টিকারী খাবার বা বস্তু বর্জন।",
                "Dermatology", "ত্বক ও রোগ প্রতিরোধ"
            ),

            // 9. Musculoskeletal & Rheumatology
            IllnessData(
                "Osteoarthritis", "অস্টিওআর্থ্রাইটিস / হাড়ের ক্ষয়জনিত বাত",
                "Most common form of arthritis, occurring when protective cartilage wears down.",
                "বয়স বা অতিরিক্ত ব্যবহারের ফলে হাড়ের সংযোগস্থলের তরুণাস্থি (Cartilage) ক্ষয়ে যাওয়া।",
                "Joint pain, stiffness (especially in morning), tenderness, loss of flexibility, bone spurs.",
                "গেঁটে ব্যথা, সকালে আড়ষ্টতা বা শক্ত হয়ে থাকা, হাড়ের নড়াচড়ায় ব্যথা ও শব্দ হওয়া।",
                "Joint aging, joint injury, obesity, repetitive stress, genetics.",
                "বয়সের ছাপ, জয়েন্টে আঘাত, অতিরিক্ত ওজন, বংশগত কারণ।",
                "Weight loss, physical therapy, pain relievers (Paracetamol/NSAIDs), joint supplements (Glucosamine).",
                "ওজন নিয়ন্ত্রণ, ফিজিক্যাল থেরাপি, পেইনকিলার, এবং গ্লুকোসামিন সাপ্লিমেন্ট।",
                "Musculoskeletal", "হাড় ও বাতব্যথা"
            ),
            IllnessData(
                "Rheumatoid Arthritis (RA)", "রিউমাটয়েড আর্থ্রাইটিস (রক্তের বাত)",
                "Chronic inflammatory disorder affecting many joints, including hands and feet.",
                "অটোহিমিউন কারণে সৃষ্ট প্রদাহজনক বাত যা হাতের ও পায়ের ছোট ছোট জোড়াকে আক্রমণ করে।",
                "Tender, warm, swollen joints, symmetrical joint stiffness, fatigue, low fever.",
                "জোড়ায় ব্যথা, গরম অনুভূত হওয়া, ফোলা, উভয় পাশের জোড়ায় একসাথে আড়ষ্টতা, ক্লান্তি, জ্বর।",
                "Autoimmune response where body's immune system attacks its own joint tissues.",
                "রোগ প্রতিরোধ ক্ষমতার ভুলত্রুটির কারণে নিজের শরীরের জয়েন্ট টিস্যুতে আক্রমণ করা।",
                "Disease-modifying antirheumatic drugs (DMARDs like Methotrexate), biologics, NSAIDs, steroids.",
                "ডিমার্ডস (মেথোট্রেক্সেট), এনএসএআইডি ও ডাক্তারের পরামর্শ অনুযায়ী আধুনিক চিকিৎসাব্যবস্থা।",
                "Musculoskeletal", "হাড় ও বাতব্যথা"
            ),
            IllnessData(
                "Sciatica", "সায়টিকা ব্যথা (Sciatica)",
                "Pain radiating along the sciatic nerve, which branches from your lower back through hips.",
                "কোমর থেকে শুরু হয়ে পা পর্যন্ত প্রসারিত সায়াটিক নার্ভে চাপ পড়ার কারণে সৃষ্ট তীব্র ব্যথা।",
                "Sharp pain shooting down from lower back through buttock and leg, numbness, tingling.",
                "কোমর থেকে শুরু হয়ে হিপ ও পা বেয়ে নিচে নামা তীব্র বিদ্যুৎ চমকানোর মতো ব্যথা ও অবশ ভাব।",
                "Herniated lumbar disc, bone spur on spine, spinal stenosis.",
                "লাম্ববার ডিস্ক প্রলাপ্স (Slip disc), মেরুদণ্ডে হাড়ের বৃদ্ধি বা স্পাইনাল স্টেনোসিস।",
                "Physical therapy, stretching, pain relievers, anti-inflammatory medications, surgical decompression if severe.",
                "ফিজিক্যাল থেরাপি, স্ট্রেচিং ব্যায়াম, পেইনকিলার ও প্রদাহনাশক ওষুধ। গুরুতর হলে সার্জারি।",
                "Musculoskeletal", "হাড় ও বাতব্যথা"
            ),

            // 10. Obstetrics & Gynecology
            IllnessData(
                "Polycystic Ovary Syndrome (PCOS)", "পলিসিস্টিক ওভারিয়ান সিন্ড্রোম (PCOS)",
                "Hormonal disorder causing enlarged ovaries with small cysts on outer edges.",
                "মহিলাদের হরমোনের ভারসাম্যহীনতার কারণে ওভারিতে ছোট ছোট সিস্ট হওয়া ও মাসিক অনিয়মিত হওয়া।",
                "Irregular periods, heavy bleeding, excess facial/body hair (hirsutism), acne, weight gain, infertility.",
                "অনিয়মিত মাসিক, অতিরিক্ত রক্তক্ষরণ, মুখে অতিরিক্ত লোম, ব্রণ, ওজন বৃদ্ধি, গর্ভধারণে সমস্যা।",
                "Hormonal imbalance, insulin resistance, genetics, high androgen levels.",
                "হরমোনের ভারসাম্যহীনতা, ইনসুলিন রেজিস্ট্যান্স, অতিরিক্ত অ্যান্ড্রোজেন হরমোন।",
                "Lifestyle modifications, weight loss, oral contraceptive pills (OCP), Metformin for insulin resistance.",
                "জীবনযাত্রার মান উন্নয়ন, ওজন কমানো, জন্মনিয়ন্ত্রণ বড়ি ও ডাক্তারের পরামর্শ অনুযায়ী চিকিৎসা।",
                "Gynecology", "স্ত্রীরোগ ও প্রসূতিবিদ্যা"
            ),
            IllnessData(
                "Gestational Diabetes", "গর্ভকালীন ডায়াবেটিস (Gestational Diabetes)",
                "Diabetes diagnosed for the first time during pregnancy.",
                "গর্ভধারণের সময় রক্তে শর্করার মাত্রা অস্বাভাবিক বেড়ে যাওয়া।",
                "Increased thirst, frequent urination, fatigue, blurred vision in pregnant women.",
                "গর্ভবতী মায়ের অতিরিক্ত তৃষ্ণা, ঘনঘন প্রস্রাব, ক্লান্তি, ঝাপসা দেখা।",
                "Hormonal changes from placenta blocking insulin action during pregnancy.",
                "গর্ভাবস্থায় প্লাসেন্টা থেকে উৎপাদিত হরমোনের প্রভাবে ইনসুলিন ঠিকমতো কাজ না করা।",
                "Dietary control, blood glucose monitoring, safe insulin therapy if diet is insufficient.",
                "শর্করা ও মিষ্টি নিয়ন্ত্রণে রেখে ডায়েট চার্ট মেনে চলা, নিয়মিত সুগার মাপা ও ইনসুলিন থেরাপি।",
                "Gynecology", "স্ত্রীরোগ ও প্রসূতিবিদ্যা"
            ),
            IllnessData(
                "Dysmenorrhea (Painful Menstrual Cramps)", "ডিসমেনোরিয়া (মাসিকের তীব্র ব্যথা)",
                "Painful menstrual periods caused by uterine muscle contractions.",
                "জরায়ুর পেশীর অতিরিক্ত সংকোচনের কারণে মাসিকের সময় তীব্র তলপেটে ব্যথা হওয়া।",
                "Throbbing or cramping pain in lower abdomen, lower back pain, nausea, headache.",
                "তলপেটে ও কোমরে তীব্র মোচড় দিয়ে ব্যথা, বমি ভাব, মাথাব্যথা, ক্লান্তি।",
                "Excess production of prostaglandins causing uterine contractions.",
                "প্রোস্টাগ্ল্যান্ডিন হরমোনের অতিমাত্রায় নিঃসরণের ফলে জরায়ুর অতিরিক্ত সংকোচন।",
                "Heat pads, NSAIDs (Ibuprofen/Mefenamic acid), regular exercise, hydration.",
                "গরম পানির সেঁক, পেইনকিলার (মেফেনামিক এসিড/আইবুপ্রোফেন), হালকা ব্যায়াম ও প্রচুর পানি পান।",
                "Gynecology", "স্ত্রীরোগ ও প্রসূতিবিদ্যা"
            ),

            // 11. Pediatrics
            IllnessData(
                "Neonatal Jaundice", "নবজাতকের জন্ডিস (Neonatal Jaundice)",
                "Yellow discoloration of a newborn baby's skin and eyes due to high bilirubin.",
                "নবজাতকের রক্তে বিলিরুবিনের মাত্রা বেড়ে যাওয়ায় ত্বক ও চোখ হলুদ হয়ে যাওয়া।",
                "Yellowish skin and sclera, poor feeding, lethargy, dark urine, pale stools.",
                "ত্বক ও চোখ হলুদ হওয়া, দুধ খেতে না চাওয়া, নিস্তেজ ভাব, গাড় প্রস্রাব।",
                "Physiological immaturity of newborn liver, ABO/Rh blood group incompatibility.",
                "নবজাতকের লিভারের অপরিপক্বতা, মায়ের সাথে রক্তের গ্রুপের অমিল (ABO/Rh incompatibility)।",
                "Phototherapy (blue light therapy), frequent breast/formula feeding, exchange transfusion if severe.",
                "ফটোথেরাপি (নীল আলোর নিচে রাখা), ঘনঘন বুকের দুধ খাওয়ানো এবং গুরুতর হলে ব্লাড ট্রান্সফিউশন।",
                "Pediatrics", "শিশু স্বাস্থ্য"
            ),
            IllnessData(
                "Infantile Diarrhea & Dehydration", "শিশুর পাতলা পায়খানা ও পানিশূন্যতা",
                "Acute gastrointestinal infection causing watery stools and rapid fluid loss in infants.",
                "শিশুদের ভাইরাল বা ব্যাকটেরিয়াল ইনফেকশনের কারণে পাতলা পায়খানা ও দ্রুত পানিশূন্যতা।",
                "Sunken eyes, dry mouth, no tears when crying, lethargy, reduced wet diapers.",
                "চোখ ভেতরে ঢুকে যাওয়া, মুখ শুকিয়ে যাওয়া, কান্নার সময় চোখের পানি না পড়া, নিস্তেজ হওয়া।",
                "Rotavirus infection, contaminated milk/water, poor hygiene.",
                "রোটাভাইরাস ইনফেকশন, দূষিত পানি বা দুধ, অস্বাস্থ্যকর পরিবেশ।",
                "Oral Rehydration Salt (ORS), Zinc syrup for 14 days, continued breastfeeding, emergency IV fluids if severe.",
                "ঘনঘন ওআরএস স্যালাইন খাওয়ানো, ১৪ দিন জিংক সিরাপ, বুকের দুধ চালিয়ে যাওয়া এবং প্রয়োজনে হাসপাতালে নেওয়া।",
                "Pediatrics", "শিশু স্বাস্থ্য"
            ),

            // 12. Ophthalmology & ENT
            IllnessData(
                "Cataract", "ছানি পড়া (Cataract)",
                "Clouding of the normally clear lens of the eye, causing blurred vision.",
                "চোখের ভেতরের লেন্স স্বাভাবিক স্বচ্ছতা হারিয়ে ঘোলাটে হয়ে যাওয়া, ফলে দৃষ্টিশক্তি কমে যায়।",
                "Blurry or cloudy vision, faded colors, glare around lights at night, poor night vision.",
                "ঝাপসা দৃষ্টি, রঙ অনুজ্জ্বল লাগা, রাতে গাড়ির আলোতে চোখে ধাঁধা লাগা, রাতে কম দেখা।",
                "Aging, diabetes, prolonged UV exposure, eye trauma, steroid use.",
                "বয়স বৃদ্ধি, ডায়াবেটিস, দীর্ঘসময় রোদে থাকা, চোখে আঘাত, স্টেরয়েড ওষুধের ব্যবহার।",
                "Surgical removal of cloudy lens and replacement with artificial intraocular lens (IOL).",
                "সার্জারির মাধ্যমে ঘোলা লেন্স অপসারণ করে কৃত্রিম লেন্স (IOL) প্রতিস্থাপন।",
                "Ophthalmology", "চোখ, কান, নাক ও গলা"
            ),
            IllnessData(
                "Acute Otitis Media (Middle Ear Infection)", "কানের মধ্যকর্ণের ইনফেকশন (Otitis Media)",
                "Infection of the air-filled space behind the eardrum, common in children.",
                "কানের পর্দার পেছনের অংশে ব্যাকটেরিয়া বা ভাইরাসের কারণে তীব্র ইনফেকশন ও পুঁজ জমা হওয়া।",
                "Ear pain, tugging at ears, trouble sleeping, fever, fluid drainage from ear, irritability.",
                "কানে তীব্র ব্যথা, কান টানাটানি করা, ঘুমে সমস্যা, জ্বর, কান দিয়ে পুঁজ বা পানি পড়া।",
                "Eustachian tube blockage due to cold, flu, or sinus infection.",
                "সর্দি, ঠাণ্ডা বা সাইনাস ইনফেকশনের কারণে ইউস্টেশিয়ান টিউব বন্ধ হয়ে যাওয়া।",
                "Antibiotics (Amoxicillin) if bacterial, pain relievers, warm compress, avoid water entry in ear.",
                "ডাক্তারের পরামর্শে অ্যান্টিবায়োটিক, পেইনকler ও কানে পানি প্রবেশ রোধ করা।",
                "Ophthalmology", "চোখ, কান, নাক ও গলা"
            )
        )
    }

    val filteredList = remember(searchQuery, isBn) {
        if (searchQuery.isBlank()) illnesses
        else illnesses.filter { 
            it.nameEn.contains(searchQuery, ignoreCase = true) || 
            it.nameBn.contains(searchQuery, ignoreCase = true) ||
            it.categoryEn.contains(searchQuery, ignoreCase = true) ||
            it.categoryBn.contains(searchQuery, ignoreCase = true) ||
            it.symptomsEn.contains(searchQuery, ignoreCase = true) ||
            it.symptomsBn.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
                shadowElevation = 0.dp
            ) {
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = if (isBn) "মেডিকেল এনসাইক্লোপিডিয়া" else "Medical Encyclopedia",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onBack()
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        actions = { LanguageToggleButton() },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                    
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(if (isBn) "রোগ, লক্ষণ বা চিকিৎসা খুঁজুন (যেমন: Asthma, Dengue, Diabetes)..." else "Search disease, symptom (e.g. Asthma, Dengue)...") },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                }
            }
        },
        bottomBar = { AppBottomBar(context = LocalContext.current, currentTab = -1, isNurseMode = true, backgroundBrush = AppGradientBrush) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = AppGradientBrush)
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList) { illness ->
                    EncyclopediaCard(data = illness, isDark = isSystemInDarkTheme(), isBn = isBn)
                }
                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
fun EncyclopediaCard(data: IllnessData, isDark: Boolean, isBn: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                expanded = !expanded
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color.White),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E).copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBn) data.categoryBn else data.categoryEn,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isBn) data.nameBn else data.nameEn,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isBn) "📖 সংক্ষিপ্ত বিবরণ (Overview):" else "📖 Overview:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (isBn) data.descBn else data.descEn,
                fontSize = 12.sp,
                color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.DarkGray,
                lineHeight = 16.sp
            )

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.3f))

                    Text(
                        text = if (isBn) "🩺 প্রধান লক্ষণসমূহ (Symptoms):" else "🩺 Symptoms:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800)
                    )
                    Text(
                        text = if (isBn) data.symptomsBn else data.symptomsEn,
                        fontSize = 12.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.DarkGray,
                        lineHeight = 16.sp
                    )

                    Text(
                        text = if (isBn) "🦠 কারণসমূহ (Causes & Risk Factors):" else "🦠 Causes & Risk Factors:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA855F7)
                    )
                    Text(
                        text = if (isBn) data.causesBn else data.causesEn,
                        fontSize = 12.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.DarkGray,
                        lineHeight = 16.sp
                    )

                    Text(
                        text = if (isBn) "💊 ক্লিনিক্যাল কেয়ার ও চিকিৎসা (Clinical Care):" else "💊 Clinical Care & Treatment:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF22C55E)
                    )
                    Text(
                        text = if (isBn) data.careBn else data.careEn,
                        fontSize = 12.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.DarkGray,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
