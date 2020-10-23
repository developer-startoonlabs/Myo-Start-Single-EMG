package com.startoonlabs.apps.pheezee.utils;

import android.content.Context;

import com.startoonlabs.apps.pheezee.R;

public class MuscleOperation {
    private final static String[][] musle_names = {
            {"Select Muscle*", "Biceps", "Brachialis (Deep)","Brachioradialis", "Tricep", "Anconeus", "Others"},//elbow

            {"Select Muscle*", "Sartorius - Anterior", "Gracilis - Medial", "Biceps Femoris - Posterior", "Semimembranosus - Posterior",
                    "Semitendinosus - Posterior", "Popliteus - Posterior (Deep)", "Gastrocnemius - Posterior", "Rectus Femoris - Anterior",
                    "Vastus Lateralis - Anterior", "Vastus Medialis - Anterior","Vastus Intermedius - Anterior (Deep)", "Others"
            }, //Knee

            {"Select Muscle*", "Gastrocnemius - Posterior", /*"superficial-Part of Triceps Surae",*/ "Soleus - Posterior", "Plantaris - Posterior",
                    "Flexor Digitorum Longus (Deep)", "Flexor Hallucis Longus (Deep)", "Tibialis Posterior (Deep)", "Tibialis Anterior",
                    "Extensor Digitorum Longus - Anterior", "Extensor Hallucis Longus - Anterior", "Peroneus Tertius - Anterior",
                    "Peroneus Longus - Lateral", "Peroneus Brevis - Lateral", "Others"},    //Ankle

            {"Select Muscle*", "Rectus Femoris - Anterior", "Sartorius - Anterior", "Pectineus - Medial (Deep)",
                    "Gluteus Medius and Gluteus Minimus - Gluteal", "Tensor Fasciae Latae - Gluteal", "Adductor Longus - Medial",
                    "Adductor Brevis - Medial","Adductor Magnus - Medial","Psoas Major","Iliacus","Gluteus Maximus - Gluteal","Biceps Femoris - Posterior",
                    "Semimembranosus - Posterior", "Semitendinosus - Posterior", "Gracilis - Medial","Piriformis (Deep)",
                    "Superior Gemellus (Deep)", "Obturator Internus (Deep)","Inferior Gemellus (Deep)", "Quadratus Femoris (Deep)","Obturator Externus (Deep)", "Others"},  //Hip //from 4 to 8 i have taken as different muscles as specified in the excell sheet

            {"Select Muscle*", "Flexor Carpi Radialis", "Palmaris Longus", "Flexor Carpi Ulnaris", "Flexor Pollicis Longus (Deep)",
                    "Flexor Digitorum Superficialis (Intermediate)", "Flexor Digitorum Profundus (Deep)", "Extensor Carpi Radialis Longus and Brevis",
                    "Extensor Digitorum", "Extensor Carpi Ulnaris", "Extensor Digiti Minimi", "Others"},  //Wrist

            {"Select Muscle*", "Pectoralis Major", "Latissimus Dorsi", "Teres Major", "Subscapularis", "Deltoid",  "Biceps",
                    "Coracobrachialis",  "Pectoralis Minor", "Serratus Anterior", "Infraspinatus", "Teres Minor", "Trapezius","Supraspinatus","Others"},   //Shoulder

            {"Select Muscle*","Biceps","Supinator (Deep)","Pronator Quadratus (Deep)", "Pronator Teres (Deep)", "Others"}, //forearm
            {"Select Muscle*","Quadratus Lumborum","Rectus Abdominis","External Oblique","Spinalis Thoracis","Spinalis Capitis",
                    "Spinalis Cervicis","Longissimus Thoracis","Longissimus Cervicis","Longissimus Capitis",
                    "Iliocostalis Cervicis", "Iliocostalis Thoracis","Iliocostalis Lumborum","Semispinalis","Multifidus","Rotatores","Others"}, //Spine
            {"Select Muscle*","Others"}
    };

    private final static String[][][] musle_names_sorted = {
            { // Shoulder - 0
                    {// Shoulder Flexion - 0

                            "Deltoid","Biceps","Coracobrachialis","Pectoralis Major", "Latissimus Dorsi", "Teres Major", "Subscapularis",
                            "Pectoralis Minor", "Serratus Anterior", "Infraspinatus", "Teres Minor", "Trapezius","Supraspinatus","Others"


                    },
                    {// Shoulder Extension - 1

                            "Latissimus Dorsi","Deltoid",
                            "Pectoralis Major", "Teres Major", "Subscapularis",  "Biceps",
                            "Coracobrachialis",  "Pectoralis Minor", "Serratus Anterior", "Infraspinatus", "Teres Minor", "Trapezius","Supraspinatus","Others"


                    },
                    {// Shoulder Isometric - 2

                            "Pectoralis Major", "Latissimus Dorsi", "Teres Major", "Subscapularis", "Deltoid",  "Biceps",
                            "Coracobrachialis",  "Pectoralis Minor", "Serratus Anterior", "Infraspinatus", "Teres Minor", "Trapezius","Supraspinatus","Others"



                    },
                    {// Shoulder Adduction - 3

                            "Pectoralis Major","Latissimus Dorsi","Teres Major","Subscapularis",
                            "Deltoid",  "Biceps",
                            "Coracobrachialis",  "Pectoralis Minor", "Serratus Anterior", "Infraspinatus", "Teres Minor", "Trapezius","Supraspinatus","Others"


                    },
                    {// Shoulder Abduction - 4

                            "Deltoid","Supraspinatus","Latissimus Dorsi",
                            "Pectoralis Major", "Teres Major",   "Biceps",
                            "Coracobrachialis",  "Pectoralis Minor", "Serratus Anterior", "Infraspinatus", "Teres Minor", "Trapezius","Supraspinatus","Others"


                    },
                    {// Medial Rotation - 5

                            "Pectoralis Major","Latissimus Dorsi","Subscapularis","Teres Major","Pectoralis Minor",
                             "Deltoid",  "Biceps",
                            "Coracobrachialis", "Serratus Anterior", "Infraspinatus", "Teres Minor", "Trapezius","Supraspinatus","Others"


                    },
                    {// Lateral Rotation - 5

                            "Infraspinatus","Teres Minor","Serratus Anterior","Trapezius",
                            "Pectoralis Major", "Latissimus Dorsi", "Teres Major", "Subscapularis", "Deltoid",  "Biceps",
                            "Coracobrachialis",  "Pectoralis Minor", "Supraspinatus","Others"


                    }

            },
            {   //Elbow - 1
                    {// Elbow Flexion - 0

                            "Biceps","Brachialis (Deep)","Brachioradialis","Tricep","Anconeus","Others"


                    },
                    {// Elbow Extension - 1

                            "Tricep","Anconeus","Biceps", "Brachialis (Deep)","Brachioradialis","Others"


                    },
                    {// Elbow Isometric - 2

                            "Biceps", "Brachialis (Deep)","Brachioradialis", "Tricep", "Anconeus", "Others"



                    }

            },
            { // Forearm - 2
                    {// Forearm Supination - 0

                            "Supinator (Deep)","Biceps","Pronator Quadratus (Deep)", "Pronator Teres (Deep)", "Others"


                    },
                    {// Forearm Pronation - 1

                            "Pronator Quadratus (Deep)","Pronator Teres (Deep)","Biceps","Supinator (Deep)","Others"


                    },
                    {// Forearm Isometric - 2

                            "Biceps","Supinator (Deep)","Pronator Quadratus (Deep)", "Pronator Teres (Deep)", "Others"
                    }

            },
            { // Wrist - 3
                    {// Wrist Flexion - 0

                            "Flexor Carpi Radialis","Flexor Carpi Ulnaris","Palmaris Longus","Flexor Pollicis Longus (Deep)","Flexor Digitorum Superficialis (Intermediate)","Flexor Digitorum Profundus (Deep)",
                              "Extensor Carpi Radialis Longus and Brevis","Extensor Digitorum", "Extensor Carpi Ulnaris", "Extensor Digiti Minimi", "Others"


                    },
                    {// Wrist Extension - 1

                            "Extensor Digitorum","Extensor Carpi Ulnaris","Extensor Carpi Radialis Longus and Brevis","Extensor Digiti Minimi",

                            "Flexor Carpi Radialis", "Palmaris Longus", "Flexor Carpi Ulnaris", "Flexor Pollicis Longus (Deep)",
                            "Flexor Digitorum Superficialis (Intermediate)", "Flexor Digitorum Profundus (Deep)", "Others"


                    },
                    {// Wrist Isometric - 2

                            "Flexor Carpi Radialis", "Palmaris Longus", "Flexor Carpi Ulnaris", "Flexor Pollicis Longus (Deep)",
                            "Flexor Digitorum Superficialis (Intermediate)", "Flexor Digitorum Profundus (Deep)", "Extensor Carpi Radialis Longus and Brevis",
                            "Extensor Digitorum", "Extensor Carpi Ulnaris", "Extensor Digiti Minimi", "Others"



                    },
                    {// Wrist Radial Deviation - 3

                            "Flexor Carpi Radialis","Extensor Carpi Radialis Longus and Brevis",

                            "Palmaris Longus", "Flexor Carpi Ulnaris", "Flexor Pollicis Longus (Deep)",
                            "Flexor Digitorum Superficialis (Intermediate)", "Flexor Digitorum Profundus (Deep)",
                            "Extensor Digitorum", "Extensor Carpi Ulnaris", "Extensor Digiti Minimi", "Others"



                    },
                    {// Wrist Ulnar Deviation - 4

                            "Flexor Carpi Ulnaris","Extensor Carpi Ulnaris",

                            "Flexor Carpi Radialis", "Palmaris Longus",  "Flexor Pollicis Longus (Deep)",
                            "Flexor Digitorum Superficialis (Intermediate)", "Flexor Digitorum Profundus (Deep)", "Extensor Carpi Radialis Longus and Brevis",
                            "Extensor Digitorum", "Extensor Digiti Minimi", "Others"



                    }

            },
            { // Ankle - 4
                    {// Ankle Plantarflexion - 0

                            "Soleus - Posterior","Gastrocnemius - Posterior","Plantaris - Posterior","Peroneus Longus - Lateral",
                            "Peroneus Brevis - Lateral","Flexor Digitorum Longus (Deep)","Flexor Hallucis Longus (Deep)","Tibialis Posterior (Deep)",

                             "Tibialis Anterior",
                            "Extensor Digitorum Longus - Anterior", "Extensor Hallucis Longus - Anterior", "Peroneus Tertius - Anterior",
                              "Others"


                    },
                    {// Ankle Dosrsiflexion - 1

                            "Tibialis Anterior","Extensor Digitorum Longus - Anterior","Extensor Hallucis Longus - Anterior","Peroneus Tertius - Anterior",

                            "Gastrocnemius - Posterior", /*"superficial-Part of Triceps Surae",*/ "Soleus - Posterior", "Plantaris - Posterior",
                            "Flexor Digitorum Longus (Deep)", "Flexor Hallucis Longus (Deep)", "Tibialis Posterior (Deep)",
                            "Peroneus Longus - Lateral", "Peroneus Brevis - Lateral", "Others"


                    },
                    {// Ankle Isometric - 2

                            "Gastrocnemius - Posterior", /*"superficial-Part of Triceps Surae",*/ "Soleus - Posterior", "Plantaris - Posterior",
                            "Flexor Digitorum Longus (Deep)", "Flexor Hallucis Longus (Deep)", "Tibialis Posterior (Deep)", "Tibialis Anterior",
                            "Extensor Digitorum Longus - Anterior", "Extensor Hallucis Longus - Anterior", "Peroneus Tertius - Anterior",
                            "Peroneus Longus - Lateral", "Peroneus Brevis - Lateral", "Others"



                    },
                    {// Ankle Inversion - 3

                            "Tibialis Anterior","Soleus - Posterior","Gastrocnemius - Posterior","Extensor Hallucis Longus - Anterior","Flexor Digitorum Longus (Deep)",
                            "Flexor Hallucis Longus (Deep)","Tibialis Posterior (Deep)",

                             "Plantaris - Posterior",
                              "Extensor Digitorum Longus - Anterior",  "Peroneus Tertius - Anterior",
                            "Peroneus Longus - Lateral", "Peroneus Brevis - Lateral", "Others"


                    },
                    {// Ankle Eversion - 4

                            "Peroneus Longus - Lateral","Peroneus Brevis - Lateral","Extensor Digitorum Longus - Anterior","Peroneus Tertius - Anterior",

                            "Gastrocnemius - Posterior", /*"superficial-Part of Triceps Surae",*/ "Soleus - Posterior", "Plantaris - Posterior",
                            "Flexor Digitorum Longus (Deep)", "Flexor Hallucis Longus (Deep)", "Tibialis Posterior (Deep)", "Tibialis Anterior",
                             "Extensor Hallucis Longus - Anterior", "Others"


                    }

            },
            { // Knee - 5
                    {// Knee Flexion - 0

                            "Gastrocnemius - Posterior","Biceps Femoris - Posterior","Sartorius - Anterior","Gracilis - Medial",
                            "Semimembranosus - Posterior","Semitendinosus - Posterior","Popliteus - Posterior (Deep)",

                             "Rectus Femoris - Anterior",
                            "Vastus Lateralis - Anterior", "Vastus Medialis - Anterior","Vastus Intermedius - Anterior (Deep)", "Others"


                    },
                    {// Knee Extension - 1

                            "Rectus Femoris - Anterior","Vastus Lateralis - Anterior", "Vastus Medialis - Anterior","Vastus Intermedius - Anterior (Deep)",

                            "Sartorius - Anterior", "Gracilis - Medial", "Biceps Femoris - Posterior", "Semimembranosus - Posterior",
                            "Semitendinosus - Posterior", "Popliteus - Posterior (Deep)", "Gastrocnemius - Posterior", "Others"



                    },
                    {// Knee Isometric - 2

                            "Sartorius - Anterior", "Gracilis - Medial", "Biceps Femoris - Posterior", "Semimembranosus - Posterior",
                            "Semitendinosus - Posterior", "Popliteus - Posterior (Deep)", "Gastrocnemius - Posterior", "Rectus Femoris - Anterior",
                            "Vastus Lateralis - Anterior", "Vastus Medialis - Anterior","Vastus Intermedius - Anterior (Deep)", "Others"


                    }

            },
            { // Hip - 6
                    {// Hip Flexion - 0

                            "Rectus Femoris - Anterior","Sartorius - Anterior","Tensor Fasciae Latae - Gluteal", "Gluteus Medius and Gluteus Minimus - Gluteal",
                            "Pectineus - Medial (Deep)","Adductor Longus - Medial","Adductor Brevis - Medial","Adductor Magnus - Medial",

                            "Psoas Major","Iliacus","Gluteus Maximus - Gluteal","Biceps Femoris - Posterior",
                            "Semimembranosus - Posterior", "Semitendinosus - Posterior", "Gracilis - Medial","Piriformis (Deep)",
                            "Superior Gemellus (Deep)", "Obturator Internus (Deep)","Inferior Gemellus (Deep)", "Quadratus Femoris (Deep)","Obturator Externus (Deep)", "Others"


                    },
                    {// Hip Extension - 1

                            "Gluteus Maximus - Gluteal","Biceps Femoris - Posterior","Gluteus Medius and Gluteus Minimus - Gluteal","Semimembranosus - Posterior",
                            "Semitendinosus - Posterior","Adductor Magnus - Medial",

                            "Rectus Femoris - Anterior", "Sartorius - Anterior", "Pectineus - Medial (Deep)",
                             "Tensor Fasciae Latae - Gluteal", "Adductor Longus - Medial",
                            "Adductor Brevis - Medial","Psoas Major","Iliacus",
                             "Gracilis - Medial","Piriformis (Deep)",
                            "Superior Gemellus (Deep)", "Obturator Internus (Deep)","Inferior Gemellus (Deep)", "Quadratus Femoris (Deep)","Obturator Externus (Deep)", "Others"


                    },
                    {// Hip Isometric - 2

                            "Rectus Femoris - Anterior", "Sartorius - Anterior", "Pectineus - Medial (Deep)",
                            "Gluteus Medius and Gluteus Minimus - Gluteal", "Tensor Fasciae Latae - Gluteal", "Adductor Longus - Medial",
                            "Adductor Brevis - Medial","Adductor Magnus - Medial","Psoas Major","Iliacus","Gluteus Maximus - Gluteal","Biceps Femoris - Posterior",
                            "Semimembranosus - Posterior", "Semitendinosus - Posterior", "Gracilis - Medial","Piriformis (Deep)",
                            "Superior Gemellus (Deep)", "Obturator Internus (Deep)","Inferior Gemellus (Deep)", "Quadratus Femoris (Deep)","Obturator Externus (Deep)", "Others"



                    },
                    {// Hip Adduction - 3

                            "Adductor Magnus - Medial","Adductor Longus - Medial","Adductor Brevis - Medial", "Pectineus - Medial (Deep)","Gracilis - Medial",

                            "Rectus Femoris - Anterior", "Sartorius - Anterior",
                            "Gluteus Medius and Gluteus Minimus - Gluteal", "Tensor Fasciae Latae - Gluteal","Psoas Major","Iliacus","Gluteus Maximus - Gluteal","Biceps Femoris - Posterior",
                            "Semimembranosus - Posterior", "Semitendinosus - Posterior", "Piriformis (Deep)",
                            "Superior Gemellus (Deep)", "Obturator Internus (Deep)","Inferior Gemellus (Deep)", "Quadratus Femoris (Deep)","Obturator Externus (Deep)", "Others"


                    },
                    {// Hip Abduction - 4

                            "Gluteus Medius and Gluteus Minimus - Gluteal","Tensor Fasciae Latae - Gluteal",

                            "Rectus Femoris - Anterior", "Sartorius - Anterior", "Pectineus - Medial (Deep)",
                            "Adductor Longus - Medial",
                            "Adductor Brevis - Medial","Adductor Magnus - Medial","Psoas Major","Iliacus","Gluteus Maximus - Gluteal","Biceps Femoris - Posterior",
                            "Semimembranosus - Posterior", "Semitendinosus - Posterior", "Gracilis - Medial","Piriformis (Deep)",
                            "Superior Gemellus (Deep)", "Obturator Internus (Deep)","Inferior Gemellus (Deep)", "Quadratus Femoris (Deep)","Obturator Externus (Deep)", "Others"


                    },
                    {// Hip Medial Rotation - 5

                            "Tensor Fasciae Latae - Gluteal","Gluteus Medius and Gluteus Minimus - Gluteal","Adductor Longus - Medial","Adductor Brevis - Medial",
                            "Adductor Magnus - Medial", "Pectineus - Medial (Deep)",

                            "Rectus Femoris - Anterior", "Sartorius - Anterior",
                            "Psoas Major","Iliacus","Gluteus Maximus - Gluteal","Biceps Femoris - Posterior",
                            "Semimembranosus - Posterior", "Semitendinosus - Posterior", "Gracilis - Medial","Piriformis (Deep)",
                            "Superior Gemellus (Deep)", "Obturator Internus (Deep)","Inferior Gemellus (Deep)", "Quadratus Femoris (Deep)","Obturator Externus (Deep)", "Others"


                    },
                    {// Hip Lateral Rotation - 6
                            // Primary
                            "Adductor Magnus - Medial" ,"Gluteus Medius and Gluteus Minimus - Gluteal","Gluteus Maximus - Gluteal","Piriformis (Deep)","Superior Gemellus (Deep)",
                            "Obturator Internus (Deep)","Inferior Gemellus (Deep)", "Quadratus Femoris (Deep)","Obturator Externus (Deep)",

                            "Rectus Femoris - Anterior", "Sartorius - Anterior", "Pectineus - Medial (Deep)",
                            "Tensor Fasciae Latae - Gluteal", "Adductor Longus - Medial",
                            "Adductor Brevis - Medial","Psoas Major","Iliacus","Biceps Femoris - Posterior",
                            "Semimembranosus - Posterior", "Semitendinosus - Posterior", "Gracilis - Medial", "Others"


                    }

            },
            { // Spine - 7
                    {// Spine Flexion - 0
                            // Primary
                            "Rectus Abdominis","External Oblique",

                            "Quadratus Lumborum","Spinalis Thoracis","Spinalis Capitis",
                            "Spinalis Cervicis","Longissimus Thoracis","Longissimus Cervicis","Longissimus Capitis",
                            "Iliocostalis Cervicis", "Iliocostalis Thoracis","Iliocostalis Lumborum","Semispinalis","Multifidus","Rotatores","Others"



                    },
                    {// Spine Extension - 1
                            // Primary
                            "Spinalis Thoracis","Spinalis Capitis","Spinalis Cervicis",
                            "Longissimus Thoracis","Longissimus Cervicis","Longissimus Capitis",
                            "Iliocostalis Cervicis", "Iliocostalis Thoracis","Iliocostalis Lumborum",
                            "Semispinalis",

                            "Quadratus Lumborum","Rectus Abdominis","External Oblique","Multifidus","Rotatores","Others"



                    },
                    {// Spine Isometric - 2
                            // Primary
                            "Quadratus Lumborum","Rectus Abdominis","External Oblique","Spinalis Thoracis","Spinalis Capitis",
                            "Spinalis Cervicis","Longissimus Thoracis","Longissimus Cervicis","Longissimus Capitis",
                            "Iliocostalis Cervicis", "Iliocostalis Thoracis","Iliocostalis Lumborum","Semispinalis","Multifidus","Rotatores","Others"



                    },
                    {// Spine Lateral Flexion - 3
                            // Primary
                            "Spinalis Thoracis","Spinalis Capitis","Spinalis Cervicis",
                            "Longissimus Thoracis","Longissimus Cervicis","Longissimus Capitis",
                            "Iliocostalis Cervicis", "Iliocostalis Thoracis","Iliocostalis Lumborum",
                            "Quadratus Lumborum",

                            "Rectus Abdominis","External Oblique","Semispinalis","Multifidus","Rotatores","Others"

                    },
                    {// Spine Rotation - 4
                            // Primary
                            "External Oblique","Multifidus","Rotatores",

                            "Quadratus Lumborum","Rectus Abdominis","Spinalis Thoracis","Spinalis Capitis",
                            "Spinalis Cervicis","Longissimus Thoracis","Longissimus Cervicis","Longissimus Capitis",
                            "Iliocostalis Cervicis", "Iliocostalis Thoracis","Iliocostalis Lumborum","Semispinalis","Others"


                    }

            }

    };

    public static String[] getMusleNames(int postion){
        return musle_names[postion];
    }

    public static String[] getMusleNames(String bodypart_str,String exercise_str){
        int bodypart=0;
        int exercise = 2;

        // Selecting Bodypart
        if(bodypart_str.equalsIgnoreCase("shoulder"))
        {
            bodypart= 0;
        } else if(bodypart_str.equalsIgnoreCase("elbow"))
        {
            bodypart= 1;
        } else if(bodypart_str.equalsIgnoreCase("forearm"))
        {
            bodypart= 2;
        } else if(bodypart_str.equalsIgnoreCase("wrist"))
        {
            bodypart= 3;
        } else if(bodypart_str.equalsIgnoreCase("ankle"))
        {
            bodypart= 4;
        } else if(bodypart_str.equalsIgnoreCase("knee"))
        {
            bodypart= 5;
        } else if(bodypart_str.equalsIgnoreCase("hip"))
        {
            bodypart= 6;
        } else if(bodypart_str.equalsIgnoreCase("spine"))
        {
            bodypart= 7;
        }


        // Selection Exercise
        if(exercise_str.equalsIgnoreCase("flexion") || exercise_str.equalsIgnoreCase("supination") || exercise_str.equalsIgnoreCase("plantarflexion"))
        {
            exercise= 0;
        }else if(exercise_str.equalsIgnoreCase("extension") || exercise_str.equalsIgnoreCase("pronation") || exercise_str.equalsIgnoreCase("dorsiflexion"))
        {
            exercise= 1;
        } else if(exercise_str.equalsIgnoreCase("isometric"))
        {
            exercise= 2;
        } else if(exercise_str.equalsIgnoreCase("adduction") || exercise_str.equalsIgnoreCase("radial deviation") || exercise_str.equalsIgnoreCase("inversion") || exercise_str.equalsIgnoreCase("lateral flexion"))
        {
            exercise= 3;
        } else if(exercise_str.equalsIgnoreCase("abduction") || exercise_str.equalsIgnoreCase("ulnar deviation") || exercise_str.equalsIgnoreCase("eversion") || exercise_str.equalsIgnoreCase("rotation"))
        {
            exercise= 4;
        } else if(exercise_str.equalsIgnoreCase("medial rotation"))
        {
            exercise= 5;
        } else if(exercise_str.equalsIgnoreCase("lateral rotation"))
        {
            exercise= 6;
        }

        return musle_names_sorted[bodypart][exercise];
    }


    private final static String[][] exercise_names = {
            {"Select Exercise*", "Flexion", "Extension", "Isometric"},//elbow

            {"Select Exercise*", "Flexion", "Extension",  "Isometric"}, //Knee

            {"Select Exercise*",  "Plantarflexion", "Dorsiflexion", "Inversion", "Eversion", "Isometric"},    //Ankle

            {"Select Exercise*", "Flexion", "Extension",  "Adduction", "Abduction", "Medial Rotation","Lateral Rotation", "Isometric"},  //Hip

            {"Select Exercise*", "Flexion", "Extension", "Radial deviation", "Ulnar deviation", "Isometric"},  //Wrist

            {"Select Exercise*", "Adduction","Abduction", "Flexion", "Extension", "Medial Rotation", "Lateral Rotation",
//                    "Protraction", "Retraction", "Elevation", "Depression",
                    "Isometric"},   //Shoulder
            {"Select Exercise*","Supination", "Pronation","Isometric"},//forearm
            {"Select Exercise*","Flexion", "Extension","Lateral Flexion","Rotation","Isometric"},//Spine
            {"Select Exercise*","Others"}
    };

    private final static String[][][][] primary_secondary_muscle_list = {
            { // Shoulder - 0
                    {// Shoulder Flexion - 0
                            { // Primary
                                    "Deltoid"

                            },
                            { // Secondary
                                    "Biceps","Coracobrachialis"
                            }

                    },
                    {// Shoulder Extension - 1
                            { // Primary
                                    "Latissimus Dorsi"

                            },
                            { // Secondary
                                    "Deltoid"
                            }

                    },
                    {// Shoulder Isometric - 2
                            { // Primary
                                    "Pectoralis Major", "Latissimus Dorsi", "Teres Major", "Subscapularis", "Deltoid",  "Biceps",
                                    "Coracobrachialis",  "Pectoralis Minor", "Serratus Anterior", "Infraspinatus", "Teres Minor", "Trapezius","Supraspinatus","Others"

                            },
                            { // Secondary
                                    ""
                            }

                    },
                    {// Shoulder Adduction - 3
                            { // Primary
                                "Pectoralis Major","Latissimus Dorsi"

                            },
                            { // Secondary
                                "Teres Major","Subscapularis"
                            }

                    },
                    {// Shoulder Abduction - 4
                            { // Primary
                                    "Deltoid","Supraspinatus"

                            },
                            { // Secondary
                                    "Latissimus Dorsi"
                            }

                    },
                    {// Medial Rotation - 5
                            { // Primary
                                    "Pectoralis Major","Latissimus Dorsi","Subscapularis"

                            },
                            { // Secondary
                                    "Teres Major","Pectoralis Minor"
                            }

                    },
                    {// Lateral Rotation - 5
                            { // Primary
                                    "Infraspinatus","Teres Minor"

                            },
                            { // Secondary
                                    "Serratus Anterior","Trapezius"
                            }

                    }

            },
            {   //Elbow - 1
                    {// Elbow Flexion - 0
                            { // Primary
                                    "Biceps"

                            },
                            { // Secondary
                                    "Brachialis (Deep)","Brachioradialis"
                            }

                    },
                    {// Elbow Extension - 1
                            { // Primary
                                    "Tricep"

                            },
                            { // Secondary
                                    "Anconeus"
                            }

                    },
                    {// Elbow Isometric - 2
                            { // Primary
                                    "Biceps", "Brachialis (Deep)","Brachioradialis", "Tricep", "Anconeus", "Others"

                            },
                            { // Secondary
                                    ""
                            }

                    }

            },
            { // Forearm - 2
                    {// Forearm Supination - 0
                            { // Primary
                                    "Supinator (Deep)"

                            },
                            { // Secondary
                                    "Biceps",
                            }

                    },
                    {// Forearm Pronation - 1
                            { // Primary
                                    "Pronator Quadratus (Deep)"

                            },
                            { // Secondary
                                    "Biceps","Pronator Teres (Deep)"
                            }

                    },
                    {// Forearm Isometric - 2
                            { // Primary
                                    "Biceps","Supinator (Deep)","Pronator Quadratus (Deep)", "Pronator Teres (Deep)", "Others"

                            },
                            { // Secondary
                                    ""
                            }

                    }

            },
            { // Wrist - 3
                    {// Wrist Flexion - 0
                            { // Primary
                                    "Flexor Carpi Radialis","Flexor Carpi Ulnaris"

                            },
                            { // Secondary
                                    "Palmaris Longus","Flexor Pollicis Longus (Deep)","Flexor Digitorum Superficialis (Intermediate)","Flexor Digitorum Profundus (Deep)"
                            }

                    },
                    {// Wrist Extension - 1
                            { // Primary
                                    "Extensor Digitorum","Extensor Carpi Ulnaris","Extensor Carpi Radialis Longus and Brevis"

                            },
                            { // Secondary
                                    "Extensor Digiti Minimi"
                            }

                    },
                    {// Wrist Isometric - 2
                            { // Primary
                                    "Flexor Carpi Radialis", "Palmaris Longus", "Flexor Carpi Ulnaris", "Flexor Pollicis Longus (Deep)",
                                    "Flexor Digitorum Superficialis (Intermediate)", "Flexor Digitorum Profundus (Deep)", "Extensor Carpi Radialis Longus and Brevis",
                                    "Extensor Digitorum", "Extensor Carpi Ulnaris", "Extensor Digiti Minimi", "Others"

                            },
                            { // Secondary
                                    ""
                            }

                    },
                    {// Wrist Radial Deviation - 3
                            { // Primary
                                    "Flexor Carpi Radialis","Extensor Carpi Radialis Longus and Brevis"

                            },
                            { // Secondary
                                    ""
                            }

                    },
                    {// Wrist Ulnar Deviation - 4
                            { // Primary
                                    "Flexor Carpi Ulnaris","Extensor Carpi Ulnaris"

                            },
                            { // Secondary
                                    ""
                            }

                    }

            },
            { // Ankle - 4
                    {// Ankle Plantarflexion - 0
                            { // Primary
                                    "Soleus - Posterior","Gastrocnemius - Posterior"

                            },
                            { // Secondary
                                    "Plantaris - Posterior","Peroneus Longus - Lateral","Peroneus Brevis - Lateral","Flexor Digitorum Longus (Deep)","Flexor Hallucis Longus (Deep)","Tibialis Posterior (Deep)"
                            }

                    },
                    {// Ankle Dosrsiflexion - 1
                            { // Primary
                                    "Tibialis Anterior"

                            },
                            { // Secondary
                                    "Extensor Digitorum Longus - Anterior","Extensor Hallucis Longus - Anterior","Peroneus Tertius - Anterior"
                            }

                    },
                    {// Ankle Isometric - 2
                            { // Primary
                                    "Gastrocnemius - Posterior", /*"superficial-Part of Triceps Surae",*/ "Soleus - Posterior", "Plantaris - Posterior",
                                    "Flexor Digitorum Longus (Deep)", "Flexor Hallucis Longus (Deep)", "Tibialis Posterior (Deep)", "Tibialis Anterior",
                                    "Extensor Digitorum Longus - Anterior", "Extensor Hallucis Longus - Anterior", "Peroneus Tertius - Anterior",
                                    "Peroneus Longus - Lateral", "Peroneus Brevis - Lateral", "Others"

                            },
                            { // Secondary
                                    ""
                            }

                    },
                    {// Ankle Inversion - 3
                            { // Primary
                                    "Tibialis Anterior"

                            },
                            { // Secondary
                                    "Soleus - Posterior","Gastrocnemius - Posterior","Extensor Hallucis Longus - Anterior","Flexor Digitorum Longus (Deep)","Flexor Hallucis Longus (Deep)","Tibialis Posterior (Deep)"
                            }

                    },
                    {// Ankle Eversion - 4
                            { // Primary
                                    "Peroneus Longus - Lateral","Peroneus Brevis - Lateral"

                            },
                            { // Secondary
                                    "Extensor Digitorum Longus - Anterior","Peroneus Tertius - Anterior"
                            }

                    }

            },
            { // Knee - 5
                    {// Knee Flexion - 0
                            { // Primary
                                    "Gastrocnemius - Posterior","Biceps Femoris - Posterior"

                            },
                            { // Secondary
                                    "Sartorius - Anterior","Gracilis - Medial","Semimembranosus - Posterior","Semitendinosus - Posterior","Popliteus - Posterior (Deep)"
                            }

                    },
                    {// Knee Extension - 1
                            { // Primary
                                    "Rectus Femoris - Anterior","Vastus Lateralis - Anterior", "Vastus Medialis - Anterior","Vastus Intermedius - Anterior (Deep)"

                            },
                            { // Secondary
                                    ""
                            }

                    },
                    {// Knee Isometric - 2
                            { // Primary
                                    "Sartorius - Anterior", "Gracilis - Medial", "Biceps Femoris - Posterior", "Semimembranosus - Posterior",
                                    "Semitendinosus - Posterior", "Popliteus - Posterior (Deep)", "Gastrocnemius - Posterior", "Rectus Femoris - Anterior",
                                    "Vastus Lateralis - Anterior", "Vastus Medialis - Anterior","Vastus Intermedius - Anterior (Deep)", "Others"

                            },
                            { // Secondary
                                    ""
                            }

                    }

            },
            { // Hip - 6
                    {// Hip Flexion - 0
                            { // Primary
                                    "Rectus Femoris - Anterior","Sartorius - Anterior","Tensor Fasciae Latae - Gluteal"

                            },
                            { // Secondary
                                    "Gluteus Medius and Gluteus Minimus - Gluteal","Pectineus - Medial (Deep)","Adductor Longus - Medial","Adductor Brevis - Medial","Adductor Magnus - Medial"
                            }

                    },
                    {// Hip Extension - 1
                            { // Primary
                                    "Gluteus Maximus - Gluteal","Biceps Femoris - Posterior"

                            },
                            { // Secondary
                                    "Gluteus Medius and Gluteus Minimus - Gluteal","Semimembranosus - Posterior", "Semitendinosus - Posterior","Adductor Magnus - Medial"
                            }

                    },
                    {// Hip Isometric - 2
                            { // Primary
                                    "Rectus Femoris - Anterior", "Sartorius - Anterior", "Pectineus - Medial (Deep)",
                                    "Gluteus Medius and Gluteus Minimus - Gluteal", "Tensor Fasciae Latae - Gluteal", "Adductor Longus - Medial",
                                    "Adductor Brevis - Medial","Adductor Magnus - Medial","Psoas Major","Iliacus","Gluteus Maximus - Gluteal","Biceps Femoris - Posterior",
                                    "Semimembranosus - Posterior", "Semitendinosus - Posterior", "Gracilis - Medial","Piriformis (Deep)",
                                    "Superior Gemellus (Deep)", "Obturator Internus (Deep)","Inferior Gemellus (Deep)", "Quadratus Femoris (Deep)","Obturator Externus (Deep)", "Others"

                            },
                            { // Secondary
                                    ""
                            }

                    },
                    {// Hip Adduction - 3
                            { // Primary
                                    "Adductor Magnus - Medial","Adductor Longus - Medial","Adductor Brevis - Medial"

                            },
                            { // Secondary
                                    "Pectineus - Medial (Deep)","Gracilis - Medial"
                            }

                    },
                    {// Hip Abduction - 4
                            { // Primary
                                    "Gluteus Medius and Gluteus Minimus - Gluteal"

                            },
                            { // Secondary
                                    "Tensor Fasciae Latae - Gluteal"
                            }

                    },
                    {// Hip Medial Rotation - 5
                            { // Primary
                                    "Tensor Fasciae Latae - Gluteal","Gluteus Medius and Gluteus Minimus - Gluteal","Adductor Longus - Medial","Adductor Brevis - Medial","Adductor Magnus - Medial"

                            },
                            { // Secondary
                                    "Pectineus - Medial (Deep)"
                            }

                    },
                    {// Hip Lateral Rotation - 6
                            { // Primary
                                    "Adductor Magnus - Medial"

                            },
                            { // Secondary
                                    "Gluteus Medius and Gluteus Minimus - Gluteal","Gluteus Maximus - Gluteal","Piriformis (Deep)","Superior Gemellus (Deep)", "Obturator Internus (Deep)","Inferior Gemellus (Deep)", "Quadratus Femoris (Deep)","Obturator Externus (Deep)"
                            }

                    }

            },
            { // Spine - 7
                    {// Spine Flexion - 0
                            { // Primary
                                    "Rectus Abdominis","External Oblique"

                            },
                            { // Secondary
                                    ""
                            }

                    },
                    {// Spine Extension - 1
                            { // Primary
                                    "Spinalis Thoracis","Spinalis Capitis","Spinalis Cervicis",
                                    "Longissimus Thoracis","Longissimus Cervicis","Longissimus Capitis",
                                    "Iliocostalis Cervicis", "Iliocostalis Thoracis","Iliocostalis Lumborum",
                                    "Semispinalis"

                            },
                            { // Secondary
                                    ""
                            }

                    },
                    {// Spine Isometric - 2
                            { // Primary
                                    "Quadratus Lumborum","Rectus Abdominis","External Oblique","Spinalis Thoracis","Spinalis Capitis",
                                    "Spinalis Cervicis","Longissimus Thoracis","Longissimus Cervicis","Longissimus Capitis",
                                    "Iliocostalis Cervicis", "Iliocostalis Thoracis","Iliocostalis Lumborum","Semispinalis","Multifidus","Rotatores","Others"

                            },
                            { // Secondary
                                    ""
                            }

                    },
                    {// Spine Lateral Flexion - 3
                            { // Primary
                                    "Spinalis Thoracis","Spinalis Capitis","Spinalis Cervicis",
                                    "Longissimus Thoracis","Longissimus Cervicis","Longissimus Capitis",
                                    "Iliocostalis Cervicis", "Iliocostalis Thoracis","Iliocostalis Lumborum",
                                    "Quadratus Lumborum"

                            },
                            { // Secondary
                                    ""
                            }

                    },
                    {// Spine Rotation - 4
                            { // Primary
                                    "External Oblique","Multifidus","Rotatores"

                            },
                            { // Secondary
                                    ""
                            }

                    }

            }

    };

    public static String[] getPrimarySecondaryMuscle(String bodypart_str,String exercise_str,int primary_secondary){

        int bodypart=0;
        int exercise=2; // By default select isometric


        // Selecting Bodypart
        if(bodypart_str.equalsIgnoreCase("shoulder"))
        {
            bodypart= 0;
        } else if(bodypart_str.equalsIgnoreCase("elbow"))
        {
            bodypart= 1;
        } else if(bodypart_str.equalsIgnoreCase("forearm"))
        {
            bodypart= 2;
        } else if(bodypart_str.equalsIgnoreCase("wrist"))
        {
            bodypart= 3;
        } else if(bodypart_str.equalsIgnoreCase("ankle"))
        {
            bodypart= 4;
        } else if(bodypart_str.equalsIgnoreCase("knee"))
        {
            bodypart= 5;
        } else if(bodypart_str.equalsIgnoreCase("hip"))
        {
            bodypart= 6;
        } else if(bodypart_str.equalsIgnoreCase("spine"))
        {
            bodypart= 7;
        }


        // Selection Exercise
        if(exercise_str.equalsIgnoreCase("flexion") || exercise_str.equalsIgnoreCase("supination") || exercise_str.equalsIgnoreCase("plantarflexion"))
        {
            exercise= 0;
        }else if(exercise_str.equalsIgnoreCase("extension") || exercise_str.equalsIgnoreCase("pronation") || exercise_str.equalsIgnoreCase("dorsiflexion"))
        {
            exercise= 1;
        } else if(exercise_str.equalsIgnoreCase("isometric"))
        {
            exercise= 2;
        } else if(exercise_str.equalsIgnoreCase("adduction") || exercise_str.equalsIgnoreCase("radial deviation") || exercise_str.equalsIgnoreCase("inversion") || exercise_str.equalsIgnoreCase("lateral flexion"))
        {
            exercise= 3;
        } else if(exercise_str.equalsIgnoreCase("abduction") || exercise_str.equalsIgnoreCase("ulnar deviation") || exercise_str.equalsIgnoreCase("eversion") || exercise_str.equalsIgnoreCase("rotation"))
        {
            exercise= 4;
        } else if(exercise_str.equalsIgnoreCase("medial rotation"))
        {
            exercise= 5;
        } else if(exercise_str.equalsIgnoreCase("lateral rotation"))
        {
            exercise= 6;
        }

        return primary_secondary_muscle_list[bodypart][exercise][primary_secondary];
    }

    public static String[] getExerciseNames(int postion){
        return exercise_names[postion];
    }


    public static int getMusclePosition(String musclename, int bodypart){
        int muscle_index = 1;
        for (int i=0;i<musle_names[bodypart].length;i++){
            if(musclename.equalsIgnoreCase(musle_names[bodypart][i])){
                muscle_index = i;
                break;
            }
        }
        return muscle_index;
    }

    public static int getExercisePosition(String exercisename, int bodypart){
        int exercise_index = 1;
        for (int i=0;i<exercise_names[bodypart].length;i++){
            if(exercisename.equalsIgnoreCase(exercise_names[bodypart][i])){
                exercise_index = i;
                break;
            }
        }
        return exercise_index;
    }

    public static int getBodypartPosition(String bodypart, Context context){
        String string_array_bodypart[] = context.getResources().getStringArray(R.array.bodyPartName);
        int body_part_position = string_array_bodypart.length-1;
        for (int i=0;i<string_array_bodypart.length;i++){
            if(bodypart.equalsIgnoreCase(string_array_bodypart[i])){
                body_part_position = i;
                break;
            }
        }
        return body_part_position;
    }

}
