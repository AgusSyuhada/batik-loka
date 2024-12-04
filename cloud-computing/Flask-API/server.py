from flask import Flask, request, jsonify
import tensorflow as tf
import numpy as np
import io
from PIL import Image

app = Flask(__name__)

# Load the TFLite model
interpreter = tf.lite.Interpreter(model_path="model.tflite")
interpreter.allocate_tensors()

# Get input and output details
input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()

TARGET_SIZE = (224, 224)
LABELS = [
    "Batik Cendrawasih", 
    "Batik Ikat Celup", 
    "Batik Insang", 
    "Batik Kawung", 
    "Batik Lasem", 
    "Batik Megamendung", 
    "Batik Pala", 
    "Batik Parang", 
    "Batik Poleng", 
    "Batik Tambal"
]

DESCRIPTIONS = {
    "Batik Cendrawasih": "Burung Cendrawasih merupakan motif yang menggambarkan burung endemik di tanah Papua. Cendrawasih adalah salah satu spesies burung langka, dilindungi oleh pemerintah Indonesia. Burung ini dipercaya sebagai burung surga yang menghubungkan kehidupan di bumi dengan surga. Motif ini juga dianggap sebagai motif sakral dan mewakili identitas masyarakat Papua, baik di provinsi Papua maupun Papua Barat.",
    "Batik Ikat Celup": "Ikat celup atau Jumputan (tie-dye) adalah teknik mewarnai kain dengan cara mengikat kain dengan cara tertentu sebelum dilakukan pencelupan. Di beberapa daerah di Indonesia, teknik ini dikenal dengan berbagai nama lain seperti jumputan pelangi atau cinde (Palembang), tritik atau jumputan (Jawa), serta sasirangan (Banjarmasin). Teknik ikat celup sering dipadukan dengan teknik lain seperti batik.",
    "Batik Insang": "Insang ikan merupakan motif yang sering dipergunakan oleh kaum Melayu yang tinggal di sepanjang tepian Sungai Kapuas. Ia melambangkan rasa syukur manusia kepada Tuhan. Insang merupakan bagian tubuh ikan yang paling penting, yang memungkinkan ikan untuk bernapas dan hidup.",
    "Batik Kawung": "Motif Batik Kawung merupakan motif batik yang bentuknya berupa bulatan mirip buah kawung (sejenis kelapa atau kadang juga dianggap sebagai aren atau kolang-kaling) yang ditata rapi secara geometris. Motif kawung bermakna kesempurnaan, kemurnian dan kesucian. Motif batik Kawung diyakini diciptakan oleh salah satu Sultan kerajaan Mataram. Motif batik ini pertama kali dikenal pada abad ke 13 tepatnya di pulau Jawa. Pada awalnya motif ini muncul pada ukiran dinding di beberapa candi di Jawa seperti Prambanan. Dalam kaitannya dengan kata suwung yang berarti kosong, motif kawung menyimbolkan kekosongan nafsu dan hasrat duniawi, sehingga menghasilkan pengendalian diri yang sempurna. Kekosongan ini menjadikan seseorang netral, tidak berpihak, tidak ingin menonjolkan diri, mengikuti arus kehidupan, membiarkan segala yang ada disekitarnya berjalan sesuai kehendak alam. Motif batik jenis kawung ini selalu dikenakan oleh semar sebagai gambaran sosok yang bijaksana.",
    "Batik Lasem": "Batik Lasem atau sering disebut Batik Laseman merupakan batik bergaya pesisiran yang kaya motif dan warna. Nuansa multikultur sangat terasa pada lembaran Batik Lasem. Kombinasi motif dan warna Batik Lasem yang terpengaruh desain budaya Tionghoa, Jawa, Lasem, Belanda, Champa, Hindu, Buddha serta Islam tampak berpadu demikian serasi, anggun dan memukau. Warna cerah Batik Lasem khususnya warna merah sangat terkenal di kalangan pecinta batik Indonesia.",
    "Batik Megamendung": "Batik megamendung merupakan karya seni berupa batik yang identik dan menjadi ikon daerah Cirebon dan sekitarnya. Motif batik tersebut memiliki kekhasan yang tidak dijumpai di wilayah lain penghasil batik, bahkan menjadi salah satu mahakarya yang hanya ada di Cirebon. Sampai saat ini, Departemen Kebudayaan dan Pariwisata terus berusaha mendaftarkan motif ini ke UNESCO untuk memperoleh penetapan sebagai salah satu warisan dunia. Sebagai motif dasar, batik megamendung telah dikenal luas hingga mancanegara. Sebagai bukti kepopulerannya, motif ini pernah dijadikan kover suatu buku batik terbitan luar negeri dengan judul Batik Design, karya seorang berkewarganegaraan Belanda yang bernama Pepin van Roojen. Kekhasan dari motif tersebut tidak hanya berada di motifnya saja yang berupa gambar awan-awan dengan warna-warna yang mencolok, tetapi juga berbagai nilai filosofis yang ada di dalamnya.",
    "Batik Pala": "Motif ini menggambarkan senjata tradisional yang unik dari wilayah Maluku, yang disebut Salawaku. Belati salawaku digunakan di banyak lingkungan sosial lokal, karena melambangkan identitas masyarakat Maluku seperti dalam tarian tradisional, ritual, dan pola tekstil. Sementara kata ‘Pala’ mengacu pada pala sebagai salah satu komoditas utama Maluku.",
    "Batik Parang": "Batik Parang adalah salah satu motif batik yang paling tua di Indonesia. Parang berasal dari kata pèrèng yang berarti lèrèng. Maksudnya, bentuk motif batik parang itu berupa huruf “S” yang digambar secara berkaitan satu sama lain dan membentuk diagonal miring layaknya lèrèng gunung. Perengan menggambarkan sebuah garis menurun dari tinggi ke rendah secara diagonal. Susunan motif S jalin-menjalin tidak terputus melambangkan kesinambungan. Bentuk dasar huruf S diambil dari ombak samudra yang menggambarkan semangat yang tidak pernah padam. Batik ini merupakan batik asli Indonesia yang sudah ada sejak zaman keraton Mataram Kartasura (Solo).",
    "Batik Poleng": "Poleng atau corak papan catur adalah pola kotak-kotak sederhana yang terbentuk dari selang-seling warna gelap dan terang, biasanya hitam dan putih.[1] Di Bali, kain dengan motif seperti ini disebut sebagai kain poleng. Kain poleng melambangkan keseimbangan antara dua hal yang bertolak belakang.[2] Corak poleng juga dikenal dalam kebudayaan Jawa, khususnya dalam kerajinan batik.",
    "Batik Tambal": "Batik tambal (aksara Jawa: ꦧꦛꦶꦏ꧀ꦠꦩ꧀ꦧꦭ꧀) adalah motif batik yang menggabungkan atau menambal berbagai macam motif batik lainnya dalam bidang-bidang segitiga yang disusun sedemikian rupa. Bidang-bidang segitiga tersebut biasanya tercipta dari bidang persegi empat yang lebih besar, dengan garis-garis yang memotong dari setiap sudutnya."
}

def prepare_image(img):
    img = img.convert('RGB')
    img = img.resize(TARGET_SIZE)
    img_array = np.array(img, dtype=np.float32)  # Convert image to float32 array
    img_array = np.expand_dims(img_array, axis=0)  # Add batch dimension
    img_array = img_array / 255.0  # Normalize the image
    return img_array

@app.route('/predict', methods=['POST'])
def predict():
    if 'input' not in request.files:
        return jsonify({'error': 'No input part'}), 400

    file = request.files['input']
    if file.filename == '':
        return jsonify({'error': 'No selected input'}), 400

    try:
        # Read and process the image
        img = Image.open(io.BytesIO(file.read()))
        img_array = prepare_image(img)

        # Set the tensor for the model input
        interpreter.set_tensor(input_details[0]['index'], img_array)

        # Run inference
        interpreter.invoke()

        # Get the output tensor
        predictions = interpreter.get_tensor(output_details[0]['index'])[0]

        # Get the index of the highest probability (class with highest confidence)
        predicted_class = np.argmax(predictions)

        # Get the label corresponding to the predicted class
        predicted_label = LABELS[predicted_class]

        # Get the description for the predicted class
        description = DESCRIPTIONS.get(predicted_label, "No description available.")

        # Return the result
        result = {
            'predictions': predictions.tolist(),  # Return all probabilities for all classes
            'label': predicted_label,  # The label for the predicted class
            'description': description  # Add a description for the predicted batik
        }

        return jsonify(result), 200
    except Exception as e:
        return jsonify({"message": f"An unexpected error occurred: {str(e)}"}), 500

@app.route('/', methods=['GET'])
@app.route('/index', methods=['GET'])
def helloworld():
    return jsonify({"message": "Service is running.."})



from script import DetikNewsApi
# Initialize the DetikNewsApi object
DN_API = DetikNewsApi()

@app.route("/news", methods=["GET"])
def search():
    # Retrieve query, detail, and limit parameters
    qs = request.args.get("q", default="batik", type=str)
    detail = request.args.get("detail", default="false", type=str).lower() in [
        "true",
        "1",
    ]
    limit = request.args.get("limit", default=None, type=int)

    if not qs:
        return (
            jsonify({"status": 400, "error": "Query parameter 'q' is required."}),
            400,
        )

    try:
        # Perform search with optional detail and limit parameters
        search_result = DN_API.search(qs, detail=detail, limit=limit)
        return (
            jsonify(
                {"status": 200, "data": search_result, "length": len(search_result)}
            ),
            200,
        )
    except Exception as e:
        return jsonify({"status": 500, "error": str(e)}), 500

if __name__ == '__main__':
    app.run(port=4000, debug=False, host='0.0.0.0')