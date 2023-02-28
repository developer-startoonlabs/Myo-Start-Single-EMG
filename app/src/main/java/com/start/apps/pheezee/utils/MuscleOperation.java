package com.start.apps.pheezee.utils;

import android.content.Context;

import start.apps.pheezee.R;

import java.util.HashMap;

public class MuscleOperation {
    static HashMap<String, Integer> Bodypart_muscle_lookuptable = new HashMap<String, Integer>(){
        {
            put("Elbow", 0);
            put("Knee", 1);
            put("Ankle", 2);
            put("Hip", 3);
            put("Wrist", 4);
            put("Shoulder", 5);
            put("Forearm", 6);
            put("Spine", 7);
            put("Cervical", 9);
            put("Thoracic", 10);
            put("Lumbar", 11);
            put("Abdomen", 8);
        }

    };
    static HashMap<String, Integer> Bodypart_exercise_lookuptable = new HashMap<String, Integer>(){
        {
            put("Shoulder", 0);
            put("Elbow", 1);
            put("Forearm", 2);
            put("Wrist", 3);
            put("Hip", 4);
            put("Knee", 5);
            put("Ankle", 6);
            put("Spine", 7);
            put("Cervical", 9);
            put("Thoracic", 10);
            put("Lumbar", 11);
            put("Abdomen", 8);
        }
    };







    private final static String[][] musle_names = {
            {"Select Muscle*", "Biceps", "Brachialis (Deep)","Brachioradialis", "Tricep", "Anconeus", "Others"},//elbow

            {"Select Muscle*", "Sartorius - Anterior", "Gracilis - Medial", "Biceps Femoris - Posterior", "Semimembranosus - Posterior",
                    "Semitendinosus - Posterior", "Popliteus - Posterior (Deep)", "Gastrocnemius - Posterior", "Rectus Femoris - Anterior",
                    "Vastus Lateralis - Anterior", "Vastus Medialis - Anterior","Vastus Intermedius - Anterior (Deep)", "Others"
            }, //Knee

            {"Select Muscle*", "Gastrocnemius - Posterior", /*"superficial-Part of Triceps Surae",*/ "Soleus - Posterior", "Plantaris - Posterior",
                    "Flexor Digitorum Longus (Deep)", "Flexor Hallucis Longus (Deep)", "Tibialis Posterior (Deep)", "Tibialis Anterior",
                    "Extensor Digitorum Longus", "Extensor Hallucis Longus - Anterior", "Peroneus Tertius - Anterior",
                    "Peroneus Longus - Lateral", "Peroneus Brevis - Lateral", "Others"},    //Ankle

            {"Select Muscle*", "Rectus Femoris - Anterior", "Sartorius - Anterior", "Pectineus - Medial (Deep)",
                    "Gluteus Medius", "Tensor Fasciae Latae - Gluteal", "Adductor Longus - Medial",
                    "Adductor Brevis - Medial","Adductor Magnus - Medial","Psoas Major","Iliacus","Gluteus Maximus - Gluteal","Biceps Femoris - Posterior",
                    "Semimembranosus - Posterior", "Semitendinosus - Posterior", "Gracilis - Medial","Piriformis (Deep)",
                    "Superior Gemellus (Deep)", "Obturator Internus (Deep)","Inferior Gemellus (Deep)", "Quadratus Femoris (Deep)","Obturator Externus (Deep)", "Others"},  //Hip //from 4 to 8 i have taken as different muscles as specified in the excell sheet

            {"Select Muscle*", "Flexor Carpi Radialis", "Palmaris Longus", "Flexor Carpi Ulnaris", "Flexor Pollicis Longus (Deep)",
                    "Flexor Digitorum Superficialis (Intermediate)", "Flexor Digitorum Profundus (Deep)", "Extensor Carpi Radialis Longus","Extensor Carpi Radialis Brevis",
                    "Extensor Digitorum", "Extensor Carpi Ulnaris", "Extensor Digiti Minimi", "Others"},  //Wrist

            {"Select Muscle*", "Pectoralis Major", "Latissimus Dorsi", "Teres Major", "Subscapularis", "Deltoid",  "Biceps",
                    "Coracobrachialis",  "Pectoralis Minor", "Serratus Anterior", "Infraspinatus", "Teres Minor", "Trapezius","Supraspinatus","Others"},   //Shoulder

            {"Select Muscle*","Biceps","Supinator (Deep)","Pronator Quadratus (Deep)", "Pronator Teres (Deep)", "Others"}, //forearm
            {"Select Muscle*","Spinalis Thoracis","Spinalis Capitis",
                    "Spinalis Cervicis","Longissimus Thoracis","Longissimus Cervicis","Longissimus Capitis",
                    "Iliocostalis Cervicis", "Iliocostalis Thoracis","Iliocostalis Lumborum","Semispinalis","Multifidus","Rotatores","Others"}, //Spine
            {"Select Muscle*","Rectus Abdominis","External Oblique","Internal Oblique","Others"}, //Abdomen
            {"Select Muscle*", "Spinalis Cervicis","Longissimus Cervicis", "Iliocostalis Cervicis", "Others"}, //Cervical
            {"Select Muscle*","Spinalis Thoracis","Longissimus Thoracis","Iliocostalis Thoracis","Semispinalis Thoracis","Others"}, //Thoracic
            {"Select Muscle*","Rectus Abdominis","Psoas major","Iliocostalis Lumborum","Quadratus Lumborum","Multifidus","Rotatores","External Oblique","Others"}, //Lumbar
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
                            "Coracobrachialis",  "Pectoralis Minor", "Serratus Anterior", "Infraspinatus", "Teres Minor", "Trapezius","Subscapularis","Others"


                    },
                    /* {// Medial Rotation - 5

                             "Pectoralis Major","Latissimus Dorsi","Subscapularis","Teres Major","Pectoralis Minor",
                              "Deltoid",  "Biceps",
                             "Coracobrachialis", "Serratus Anterior", "Infraspinatus", "Teres Minor", "Trapezius","Supraspinatus","Others"


                     },*/
                    /*{// Lateral Rotation - 5

                            "Infraspinatus","Teres Minor","Serratus Anterior","Trapezius",
                            "Pectoralis Major", "Latissimus Dorsi", "Teres Major", "Subscapularis", "Deltoid",  "Biceps",
                            "Coracobrachialis",  "Pectoralis Minor", "Supraspinatus","Others"


                    }*/

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
                            "Extensor Carpi Radialis Longus","Extensor Carpi Radialis Brevis","Extensor Digitorum", "Extensor Carpi Ulnaris", "Extensor Digiti Minimi", "Others"


                    },
                    {// Wrist Extension - 1

                            "Extensor Digitorum","Extensor Carpi Ulnaris","Extensor Carpi Radialis Longus","Extensor Carpi Radialis Brevis"/*,"Extensor Carpi Radialis Longus and Brevis"*/,"Extensor Digiti Minimi",

                            "Flexor Carpi Radialis", "Palmaris Longus", "Flexor Carpi Ulnaris", "Flexor Pollicis Longus (Deep)",
                            "Flexor Digitorum Superficialis (Intermediate)", "Flexor Digitorum Profundus (Deep)", "Others"


                    },
                    {// Wrist Isometric - 2

                            "Flexor Carpi Radialis", "Palmaris Longus", "Flexor Carpi Ulnaris", "Flexor Pollicis Longus (Deep)",
                            "Flexor Digitorum Superficialis (Intermediate)", "Flexor Digitorum Profundus (Deep)", "Extensor Carpi Radialis Longus","Extensor Carpi Radialis Brevis"/*, "Extensor Carpi Radialis Longus and Brevis"*/,
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

                            "Soleus","Gastrocnemius","Plantaris","Peroneus Longus",
                            "Peroneus Brevis","Flexor Digitorum Longus (Deep)","Flexor Hallucis Longus (Deep)","Tibialis Posterior (Deep)",

                            "Tibialis Anterior",
                            "Extensor Digitorum Longus", "Extensor Hallucis Longus", "Peroneus Tertius",
                            "Others"


                    },
                    {// Ankle Dosrsiflexion - 1

                            "Tibialis Anterior","Extensor Digitorum Longus","Extensor Hallucis Longus","Peroneus Tertius",

                            "Gastrocnemius", /*"superficial-Part of Triceps Surae",*/ "Soleus", "Plantaris",
                            "Flexor Digitorum Longus (Deep)", "Flexor Hallucis Longus (Deep)", "Tibialis Posterior (Deep)",
                            "Peroneus Longus - Lateral", "Peroneus Brevis - Lateral", "Others"


                    },
                    {// Ankle Isometric - 2

                            "Gastrocnemius", /*"superficial-Part of Triceps Surae",*/ "Soleus", "Plantaris",
                            "Flexor Digitorum Longus (Deep)", "Flexor Hallucis Longus (Deep)", "Tibialis Posterior (Deep)", "Tibialis Anterior",
                            "Extensor Digitorum Longus", "Extensor Hallucis Longus", "Peroneus Tertius",
                            "Peroneus Longus", "Peroneus Brevis", "Others"



                    },
                    {// Ankle Inversion - 3

                            "Tibialis Anterior","Soleus","Gastrocnemius","Extensor Hallucis Longus","Flexor Digitorum Longus (Deep)",
                            "Flexor Hallucis Longus (Deep)","Tibialis Posterior (Deep)",

                            "Plantaris",
                            "Extensor Digitorum Longus",  "Peroneus Tertius",
                            "Peroneus Longus", "Peroneus Brevis", "Others"


                    },
                    {// Ankle Eversion - 4

                            "Peroneus Longus","Peroneus Brevis - Lateral","Extensor Digitorum Longus","Peroneus Tertius",

                            "Gastrocnemius", /*"superficial-Part of Triceps Surae",*/ "Soleus", "Plantaris",
                            "Flexor Digitorum Longus (Deep)", "Flexor Hallucis Longus (Deep)", "Tibialis Posterior (Deep)", "Tibialis Anterior",
                            "Extensor Hallucis Longus", "Others"


                    }

            },
            { // Knee - 5
                    {// Knee Flexion - 0

                            "Gastrocnemius","Biceps Femoris","Sartorius","Gracilis",
                            "Semimembranosus","Semitendinosus","Popliteus", "Rectus Femoris",
                            "Vastus Lateralis", "Vastus Medialis","Vastus Intermedius", "Others"


                    },
                    {// Knee Extension - 1

                            "Rectus Femoris","Vastus Lateralis", "Vastus Medialis","Vastus Intermedius",
                            "Sartorius", "Gracilis", "Biceps Femoris", "Semimembranosus",
                            "Semitendinosus", "Popliteus", "Gastrocnemius", "Others"



                    },
                    {// Knee Isometric - 2

                            "Sartorius", "Gracilis", "Biceps Femoris", "Semimembranosus",
                            "Semitendinosus", "Popliteus", "Gastrocnemius", "Rectus Femoris",
                            "Vastus Lateralis", "Vastus Medialis","Vastus Intermedius", "Others"


                    }

            },
            { // Hip - 6
                    {// Hip Flexion - 0

                            "Tensor Fasciae Latae","Rectus Femoris","Sartorius", "Gluteus Medius",
                            "Pectineus (Deep)","Adductor Longus","Adductor Brevis","Adductor Magnus",
                            "Psoas Major","Iliacus","Gluteus Maximus","Biceps Femoris",
                            "Semimembranosus", "Semitendinosus", "Gracilis","Piriformis (Deep)",
                            "Superior Gemellus (Deep)", "Obturator Internus (Deep)","Inferior Gemellus (Deep)", "Quadratus Femoris (Deep)","Obturator Externus (Deep)", "Others"


                    },
                    {// Hip Extension - 1

                            "Gluteus Maximus","Biceps Femoris","Gluteus Medius","Semimembranosus",
                            "Semitendinosus","Adductor Magnus",
                            "Rectus Femoris", "Sartorius", "Pectineus (Deep)",
                            "Tensor Fasciae Latae", "Adductor Longus",
                            "Adductor Brevis","Psoas Major","Iliacus",
                            "Gracilis","Piriformis (Deep)",
                            "Superior Gemellus (Deep)", "Obturator Internus (Deep)","Inferior Gemellus (Deep)", "Quadratus Femoris (Deep)","Obturator Externus (Deep)", "Others"


                    },
                    {// Hip Isometric - 2

                            "Rectus Femoris", "Sartorius", "Pectineus - Medial (Deep)",
                            "Gluteus Medius", "Tensor Fasciae Latae", "Adductor Longus",
                            "Adductor Brevis","Adductor Magnus","Psoas Major","Iliacus","Gluteus Maximus","Biceps Femoris",
                            "Semimembranosus", "Semitendinosus", "Gracilis","Piriformis (Deep)",
                            "Superior Gemellus (Deep)", "Obturator Internus (Deep)","Inferior Gemellus (Deep)", "Quadratus Femoris (Deep)","Obturator Externus (Deep)", "Others"



                    },
                    {// Hip Adduction - 3

                            "Adductor Magnus","Adductor Longus","Adductor Brevis", "Pectineus (Deep)","Gracilis",
                            "Rectus Femoris", "Sartorius",
                            "Gluteus Medius", "Tensor Fasciae Latae","Psoas Major","Iliacus","Gluteus Maximus","Biceps Femoris",
                            "Semimembranosus", "Semitendinosus", "Piriformis (Deep)",
                            "Superior Gemellus (Deep)", "Obturator Internus (Deep)","Inferior Gemellus (Deep)", "Quadratus Femoris (Deep)","Obturator Externus (Deep)", "Others"


                    },
                    {// Hip Abduction - 4

                            "Gluteus Medius","Tensor Fasciae Latae",

                            "Rectus Femoris", "Sartorius", "Pectineus (Deep)",
                            "Adductor Longus",
                            "Adductor Brevis","Adductor Magnus","Psoas Major","Iliacus","Gluteus Maximus","Biceps Femoris",
                            "Semimembranosus", "Semitendinosus", "Gracilis","Piriformis (Deep)",
                            "Superior Gemellus (Deep)", "Obturator Internus (Deep)","Inferior Gemellus (Deep)", "Quadratus Femoris (Deep)","Obturator Externus (Deep)", "Others"


                    },
                    {// Hip Medial Rotation - 5

                            "Tensor Fasciae Latae - Gluteal","Gluteus Medius","Adductor Longus - Medial","Adductor Brevis - Medial",
                            "Adductor Magnus - Medial", "Pectineus - Medial (Deep)",

                            "Rectus Femoris - Anterior", "Sartorius - Anterior",
                            "Psoas Major","Iliacus","Gluteus Maximus - Gluteal","Biceps Femoris - Posterior",
                            "Semimembranosus - Posterior", "Semitendinosus - Posterior", "Gracilis - Medial","Piriformis (Deep)",
                            "Superior Gemellus (Deep)", "Obturator Internus (Deep)","Inferior Gemellus (Deep)", "Quadratus Femoris (Deep)","Obturator Externus (Deep)", "Others"


                    },
                    {// Hip Lateral Rotation - 6
                            // Primary
                            "Adductor Magnus - Medial" ,"Gluteus Medius","Gluteus Maximus - Gluteal","Piriformis (Deep)","Superior Gemellus (Deep)",
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


                            "Spinalis Thoracis","Spinalis Capitis",
                            "Spinalis Cervicis","Longissimus Thoracis","Longissimus Cervicis","Longissimus Capitis",
                            "Iliocostalis Cervicis", "Semispinalis Thoracis","Iliocostalis Thoracis","Iliocostalis Lumborum","Semispinalis","Multifidus","Rotatores","Others"



                    },
                    {// Spine Extension - 1
                            // Primary
                            "Spinalis Thoracis","Spinalis Capitis","Spinalis Cervicis",
                            "Longissimus Thoracis","Longissimus Cervicis","Longissimus Capitis",
                            "Iliocostalis Cervicis","Semispinalis Thoracis", "Iliocostalis Thoracis","Iliocostalis Lumborum",
                            "Semispinalis",

                            "Multifidus","Rotatores","Semispinalis Thoracis","Others"



                    },
                    {// Spine Isometric - 2
                            // Primary
                            "Spinalis Thoracis","Spinalis Capitis",
                            "Spinalis Cervicis","Longissimus Thoracis","Longissimus Cervicis","Longissimus Capitis",
                            "Iliocostalis Cervicis","Semispinalis Thoracis", "Iliocostalis Thoracis","Iliocostalis Lumborum","Semispinalis","Multifidus","Rotatores","Semispinalis Thoracis","Others"



                    },
                    {// Spine Lateral Flexion - 3
                            // Primary
                            "Spinalis Thoracis","Spinalis Capitis","Spinalis Cervicis",
                            "Longissimus Thoracis","Longissimus Cervicis","Longissimus Capitis",
                            "Iliocostalis Cervicis","Semispinalis Thoracis", "Iliocostalis Thoracis","Iliocostalis Lumborum",


                            "Semispinalis","Multifidus","Rotatores","Semispinalis Thoracis","Others"

                    },
                    {// Spine Rotation - 4
                            // Primary
                            "Multifidus","Rotatores",

                            "Spinalis Thoracis","Spinalis Capitis",
                            "Spinalis Cervicis","Longissimus Thoracis","Longissimus Cervicis","Longissimus Capitis",
                            "Iliocostalis Cervicis", "Semispinalis Thoracis","Iliocostalis Thoracis","Iliocostalis Lumborum","Semispinalis","Semispinalis Thoracis","Others"


                    }

            },
            { // Abdomen - 7
                    {// Abdomen Flexion - 0
                            // Primary
                            "Rectus Abdominis","External Oblique","Internal Oblique","Others"



                    },
                    {// Abdomen Extension - 1
                            // Primary
                            "Rectus Abdominis","External Oblique","Internal Oblique","Others"



                    },
                    {// Abdomen Isometric - 2
                            // Primary
                            "Rectus Abdominis","External Oblique","Internal Oblique","Others"



                    },
                    {// Abdomen Lateral Flexion - 3
                            // Primary
                            "Rectus Abdominis","External Oblique","Internal Oblique","Others"

                    },
                    {// Abdomen Rotation - 4
                            // Primary
                            "Rectus Abdominis","External Oblique","Internal Oblique","Others"


                    }

            },
            { // Cervical - 7
                    {// Cervical Flexion - 0
                            // Primary
                            "Spinalis Cervicis","Longissimus Cervicis", "Iliocostalis Cervicis", "Others"
                    },
                    {// Cervical Extension - 1
                            // Primary
                            "Spinalis Cervicis","Longissimus Cervicis", "Iliocostalis Cervicis", "Others"



                    },
                    {// Cervical Isometric - 2
                            // Primary
                            "Spinalis Cervicis","Longissimus Cervicis", "Iliocostalis Cervicis", "Others"



                    },
                    {// Cervical Lateral Flexion - 3
                            // Primary
                            "Spinalis Cervicis","Longissimus Cervicis", "Iliocostalis Cervicis", "Others"

                    },
                    {// Cervical Rotation - 4
                            // Primary
                            "Spinalis Cervicis","Longissimus Cervicis", "Iliocostalis Cervicis", "Others"


                    }

            },
            { // Thoracic - 7
                    {// Thoracic Flexion - 0
                            // Primary
                            "Spinalis Thoracis","Longissimus Thoracis","Iliocostalis Thoracis","Semispinalis Thoracis","Others"


                    },
                    {// Thoracic Extension - 1
                            // Primary
                            "Spinalis Thoracis","Longissimus Thoracis","Iliocostalis Thoracis","Semispinalis Thoracis","Others"



                    },
                    {// Thoracic Isometric - 2
                            // Primary
                            "Spinalis Thoracis","Longissimus Thoracis","Iliocostalis Thoracis","Semispinalis Thoracis","Others"



                    },
                    {// Thoracic Lateral Flexion - 3
                            // Primary
                            "Spinalis Thoracis","Longissimus Thoracis","Iliocostalis Thoracis","Semispinalis Thoracis","Others"

                    },
                    {// Thoracic Rotation - 4
                            // Primary
                            "Spinalis Thoracis","Longissimus Thoracis","Iliocostalis Thoracis","Semispinalis Thoracis","Others"


                    }

            },
            { // Lumbar - 7
                    {// Lumbar Flexion - 0
                            // Primary
                            "Rectus Abdominis","Psoas major","Iliocostalis Lumborum","Quadratus Lumborum","Multifidus","Rotatores","External Oblique","Others"



                    },
                    {// Lumbar Extension - 1
                            // Primary
                            "Rectus Abdominis","Psoas major","Iliocostalis Lumborum","Quadratus Lumborum","Multifidus","Rotatores","External Oblique","Others"



                    },
                    {// Lumbar Isometric - 2
                            // Primary
                            "Rectus Abdominis","Psoas major","Iliocostalis Lumborum","Quadratus Lumborum","Multifidus","Rotatores","External Oblique","Others"



                    },
                    {// Lumbar Lateral Flexion - 3
                            // Primary
                            "Rectus Abdominis","Psoas major","Iliocostalis Lumborum","Quadratus Lumborum","Multifidus","Rotatores","External Oblique","Others"

                    },
                    {// Lumbar Rotation - 4
                            // Primary
                            "Rectus Abdominis","Psoas major","Iliocostalis Lumborum","Quadratus Lumborum","Multifidus","Rotatores","External Oblique","Others"


                    }

            }

    };

    public static String[] getMusleNames(String bodypart){
        try {
            return musle_names[Bodypart_muscle_lookuptable.get(bodypart)];
        }catch (Exception e)
        {
            return musle_names[0];
        }
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
        } else if( bodypart_str.equalsIgnoreCase("cervical") )
        {
            bodypart= 9;
        } else if( bodypart_str.equalsIgnoreCase("thoracic"))
        {
            bodypart= 10;
        } else if( bodypart_str.equalsIgnoreCase("lumbar"))
        {
            bodypart= 11;
        } else if(bodypart_str.equalsIgnoreCase("abdomen"))
        {
            bodypart= 8;
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
            {"Select Movement*","Flexion","Extension", "Abduction","Adduction", /*"Flexion",*/ /*"Extension",*/ /*"Medial Rotation", "Lateral Rotation",*/
//                    "Protraction", "Retraction", "Elevation", "Depression",
                    "Isometric"},   //Shoulder
            {"Select Movement*", "Flexion", "Extension", "Isometric"},//elbow

            {"Select Movement*","Supination", "Pronation","Isometric"},//forearm

            {"Select Movement*", "Flexion", "Extension",/* "Radial deviation", "Ulnar deviation",*/ "Isometric"},  //Wrist

            {"Select Movement*", "Flexion", "Extension", "Abduction","Adduction"/*"Medial Rotation","Lateral Rotation"*/, "Isometric"},  //Hip

            {"Select Movement*", "Flexion", "Extension",  "Isometric"}, //Knee

            {"Select Movement*", "Plantarflexion", "Dorsiflexion", "Inversion", "Eversion", "Isometric"},    //Ankle

            {"Select Movement*","Flexion", "Extension","Lateral Flexion","Rotation","Isometric"},//Spine
            {"Select Movement*","Flexion", "Lateral Flexion","Rotation","Isometric"},//Abdomen
            {"Select Movement*","Flexion", "Extension","Lateral Flexion","Rotation","Isometric"},//Cervical
            {"Select Movement*","Flexion", "Extension","Lateral Flexion","Rotation","Isometric"},//Thoracic
            {"Select Movement*","Flexion", "Extension","Lateral Flexion","Rotation","Isometric"},//Lumbar
            {"Select Movement*","Others"}
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
                                    "Pronator Teres (Deep)",
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
                                    "Extensor Digitorum","Extensor Carpi Ulnaris","Extensor Carpi Radialis Longus","Extensor Carpi Radialis Brevis"/*,"Extensor Carpi Radialis Longus and Brevis"*/

                            },
                            { // Secondary
                                    "Extensor Digiti Minimi"
                            }

                    },
                    {// Wrist Isometric - 2
                            { // Primary
                                    "Flexor Carpi Radialis", "Palmaris Longus", "Flexor Carpi Ulnaris", "Flexor Pollicis Longus (Deep)",
                                    "Flexor Digitorum Superficialis (Intermediate)", "Flexor Digitorum Profundus (Deep)", "Extensor Carpi Radialis Longus","Extensor Carpi Radialis Brevis"/*,"Extensor Carpi Radialis Longus and Brevis"*/,
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
                                    "Soleus","Gastrocnemius"

                            },
                            { // Secondary
                                    "Plantaris","Peroneus Longus","Peroneus Brevis","Flexor Digitorum Longus (Deep)","Flexor Hallucis Longus (Deep)","Tibialis Posterior (Deep)"
                            }

                    },
                    {// Ankle Dosrsiflexion - 1
                            { // Primary
                                    "Tibialis Anterior"

                            },
                            { // Secondary
                                    "Extensor Digitorum Longus","Extensor Hallucis Longus","Peroneus Tertius"
                            }

                    },
                    {// Ankle Isometric - 2
                            { // Primary
                                    "Gastrocnemius", /*"superficial-Part of Triceps Surae",*/ "Soleus", "Plantaris",
                                    "Flexor Digitorum Longus (Deep)", "Flexor Hallucis Longus (Deep)", "Tibialis Posterior (Deep)", "Tibialis Anterior",
                                    "Extensor Digitorum Longus", "Extensor Hallucis Longus", "Peroneus Tertius",
                                    "Peroneus Longus", "Peroneus Brevis", "Others"

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
                                    "Soleus","Gastrocnemius","Extensor Hallucis Longus","Flexor Digitorum Longus (Deep)","Flexor Hallucis Longus (Deep)","Tibialis Posterior (Deep)"
                            }

                    },
                    {// Ankle Eversion - 4
                            { // Primary
                                    "Peroneus Longus","Peroneus Brevis - Lateral"

                            },
                            { // Secondary
                                    "Extensor Digitorum Longus","Peroneus Tertius"
                            }

                    }

            },
            { // Knee - 5
                    {// Knee Flexion - 0
                            { // Primary
                                    "Gastrocnemius","Biceps Femoris"

                            },
                            { // Secondary
                                    "Sartorius","Gracilis","Semimembranosus","Semitendinosus","Popliteus"
                            }

                    },
                    {// Knee Extension - 1
                            { // Primary
                                    "Rectus Femoris","Vastus Lateralis", "Vastus Medialis","Vastus Intermedius"

                            },
                            { // Secondary
                                    ""
                            }

                    },
                    {// Knee Isometric - 2
                            { // Primary
                                    "Sartorius", "Gracilis", "Biceps Femoris", "Semimembranosus",
                                    "Semitendinosus", "Popliteus", "Gastrocnemius", "Rectus Femoris",
                                    "Vastus Lateralis", "Vastus Medialis","Vastus Intermedius", "Others"

                            },
                            { // Secondary
                                    ""
                            }

                    }

            },
            { // Hip - 6
                    {// Hip Flexion - 0
                            { // Primary
                                    "Rectus Femoris","Sartorius","Tensor Fasciae Latae",

                            },
                            { // Secondary
                                    "Gluteus Medius","Pectineus (Deep)","Adductor Longus","Adductor Brevis","Adductor Magnus","Psoas Major","Iliacus"
                            }

                    },
                    {// Hip Extension - 1
                            { // Primary
                                    "Gluteus Maximus","Biceps Femoris"

                            },
                            { // Secondary
                                    "Gluteus Medius","Semimembranosus", "Semitendinosus","Adductor Magnus"
                            }

                    },
                    {// Hip Isometric - 2
                            { // Primary
                                    "Rectus Femoris", "Sartorius", "Pectineus - Medial (Deep)",
                                    "Gluteus Medius", "Tensor Fasciae Latae", "Adductor Longus",
                                    "Adductor Brevis","Adductor Magnus","Psoas Major","Iliacus","Gluteus Maximus","Biceps Femoris",
                                    "Semimembranosus", "Semitendinosus", "Gracilis","Piriformis (Deep)",
                                    "Superior Gemellus (Deep)", "Obturator Internus (Deep)","Inferior Gemellus (Deep)", "Quadratus Femoris (Deep)","Obturator Externus (Deep)", "Others"

                            },
                            { // Secondary
                                    ""
                            }

                    },
                    {// Hip Adduction - 3
                            { // Primary
                                    "Adductor Magnus","Adductor Longus","Adductor Brevis"

                            },
                            { // Secondary
                                    "Pectineus (Deep)","Gracilis"
                            }

                    },
                    {// Hip Abduction - 4
                            { // Primary
                                    "Gluteus Medius"

                            },
                            { // Secondary
                                    "Tensor Fasciae Latae"
                            }

                    },
                    {// Hip Medial Rotation - 5
                            { // Primary
                                    "Tensor Fasciae Latae - Gluteal","Gluteus Medius","Adductor Longus - Medial","Adductor Brevis - Medial","Adductor Magnus - Medial"

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
                                    "Gluteus Medius","Gluteus Maximus - Gluteal","Piriformis (Deep)","Superior Gemellus (Deep)", "Obturator Internus (Deep)","Inferior Gemellus (Deep)", "Quadratus Femoris (Deep)","Obturator Externus (Deep)"
                            }

                    }

            },
            { // Spine - 7
                    {// Spine Flexion - 0
                            { // Primary
                                    ""

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
                                    "Spinalis Thoracis","Spinalis Capitis",
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


                            },
                            { // Secondary
                                    ""
                            }

                    },
                    {// Spine Rotation - 4
                            { // Primary
                                    "Multifidus","Rotatores"

                            },
                            { // Secondary
                                    ""
                            }

                    }

            },
            { // Abdomen - 7
                    {// Abdomen Flexion - 0
                            { // Primary
                                    "Rectus Abdominis","External Oblique"

                            },
                            { // Secondary
                                    ""
                            }

                    },
                    {// Abdomen Extension - 1
                            { // Primary
                                    ""

                            },
                            { // Secondary
                                    ""
                            }

                    },
                    {// Abdomen Isometric - 2
                            { // Primary
                                    "Rectus Abdominis","External Oblique"

                            },
                            { // Secondary
                                    ""
                            }

                    },
                    {// Abdomen Lateral Flexion - 3
                            { // Primary
                                    "Rectus Abdominis","External Oblique"

                            },
                            { // Secondary
                                    ""
                            }

                    },
                    {// Abdomen Rotation - 4
                            { // Primary
                                    "External Oblique"

                            },
                            { // Secondary
                                    ""
                            }

                    }

            },
            { // Cervical - 7
                    {// Cervical Flexion - 0
                            { // Primary
                                    ""

                            },
                            { // Secondary
                                    ""
                            }

                    },
                    {// Cervical Extension - 1
                            { // Primary
                                    "Spinalis Cervicis","Longissimus Cervicis", "Iliocostalis Cervicis"
                            },
                            { // Secondary
                                    ""
                            }

                    },
                    {// Cervical Isometric - 2
                            { // Primary
                                    "Spinalis Cervicis","Longissimus Cervicis", "Iliocostalis Cervicis"
                            },
                            { // Secondary
                                    ""
                            }

                    },
                    {// Cervical Lateral Flexion - 3
                            { // Primary
                                    "Spinalis Cervicis","Longissimus Cervicis", "Iliocostalis Cervicis"

                            },
                            { // Secondary
                                    ""
                            }

                    },
                    {// Cervical Rotation - 4
                            { // Primary
                                    "Spinalis Cervicis","Longissimus Cervicis", "Iliocostalis Cervicis"

                            },
                            { // Secondary
                                    ""
                            }

                    }

            },
            { // Thoracic - 7
                    {// Thoracic Flexion - 0
                            { // Primary
                                    ""

                            },
                            { // Secondary
                                    ""
                            }

                    },
                    {// Thoracic Extension - 1
                            { // Primary
                                    "Spinalis Thoracis","Longissimus Thoracis","Iliocostalis Thoracis"

                            },
                            { // Secondary
                                    ""
                            }

                    },
                    {// Thoracic Isometric - 2
                            { // Primary
                                    "Spinalis Thoracis","Longissimus Thoracis","Iliocostalis Thoracis"
                            },
                            { // Secondary
                                    "Semispinalis Thoracis","Others"
                            }

                    },
                    {// Thoracic Lateral Flexion - 3
                            { // Primary
                                    ""

                            },
                            { // Secondary
                                    ""
                            }

                    },
                    {// Thoracic Rotation - 4
                            { // Primary
                                    ""

                            },
                            { // Secondary
                                    ""
                            }

                    }

            },
            { // Lumbar - 7
                    {// Lumbar Flexion - 0
                            { // Primary
                                    "Rectus Abdominis","Psoas major"

                            },
                            { // Secondary
                                    ""
                            }

                    },
                    {// Lumbar Extension - 1
                            { // Primary
                                    "Iliocostalis Lumborum","Quadratus Lumborum",

                            },
                            { // Secondary
                                    ""
                            }

                    },
                    {// Lumbar Isometric - 2
                            { // Primary
                                    "Rectus Abdominis","Psoas major","Iliocostalis Lumborum","Quadratus Lumborum","Multifidus","Rotatores","External Oblique"                  },
                            { // Secondary
                                    ""
                            }

                    },
                    {// Lumbar Lateral Flexion - 3
                            { // Primary
                                    "Psoas major","Iliocostalis Lumborum","Quadratus Lumborum","External Oblique"

                            },
                            { // Secondary
                                    ""
                            }

                    },
                    {// Lumbar Rotation - 4
                            { // Primary
                                    "Multifidus","Rotatores","External Oblique"

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
        } else if( bodypart_str.equalsIgnoreCase("cervical") )
        {
            bodypart= 9;
        } else if( bodypart_str.equalsIgnoreCase("thoracic"))
        {
            bodypart= 10;
        } else if( bodypart_str.equalsIgnoreCase("lumbar"))
        {
            bodypart= 11;
        }  else if(bodypart_str.equalsIgnoreCase("abdomen"))
        {
            bodypart= 8;
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

    public static String[] getExerciseNames(String bodypart){
        try {
            return exercise_names[Bodypart_exercise_lookuptable.get(bodypart)];
        }catch (Exception e)
        {
            return exercise_names[0];
        }
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

    public static int getExercisePosition(String exercisename, String bodypart){
        int exercise_index = 1;
        for (int i=0;i<exercise_names[Bodypart_exercise_lookuptable.get(bodypart)].length;i++){
            if(exercisename.equalsIgnoreCase(exercise_names[Bodypart_exercise_lookuptable.get(bodypart)][i])){
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
