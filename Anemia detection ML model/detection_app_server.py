from flask import Flask, request, jsonify, render_template
import streamlit as st
import pandas as pd
import pickle

# Load the trained model
model_path = 'random_forest_model.pkl'  # Path to your trained model
model = pickle.load(open(model_path, 'rb'))

# Define the function to preprocess input data
def preprocess_data(hemoglobin, gender, mcv):
    # Convert gender to numeric value
    gender_mapping = {'Male': 0, 'Female': 1}
    gender = gender_mapping.get(gender, 0)  # Default to 0 if gender is not found

    # Create a dataframe with the input data
    data = {'Gender': [gender], 'Hemoglobin': [hemoglobin], 'MCV': [mcv]}
    df = pd.DataFrame(data)

    return df

# Define the function to predict anemia
def predict_anemia(hemoglobin, gender, mcv):
    # Preprocess the input data
    df = preprocess_data(hemoglobin, gender, mcv)

    # Predict anemia using the trained model
    prediction = model.predict(df)

    # Return the prediction
    return prediction[0]

# Create the Streamlit app
def main():
    # Set the title and description
    st.title("Anemia Detection")
    st.write("This app helps detect anemia based on input values of hemoglobin, gender, and MCV.")
    # Add a divider
    st.markdown("<hr>", unsafe_allow_html=True)
    st.write("The dataset consists of 1421 samples with six attributes: gender, hemoglobin, mean corpuscular hemoglobin (MCH), mean corpuscular hemoglobin concentration (MCHC), mean corpuscular volume (MCV), and result.")
    st.write("On conducting six different supervised learning algorithms, Random Forest was found to outperform the others. Hence, this app is built using Random Forest for anemia prediction.")

    
    # Display average range table
    st.write("Average Range of Hemoglobin and MCV (in units):")
    average_range = {'Gender': ['Male', 'Female'],
                     'Hemoglobin': ['12.0 - 15.0 g/dL', '11.5 - 14.5 g/dL'],
                     'MCV': ['80.0 - 95.0 fL', '82.0 - 98.0 fL']}
    df_range = pd.DataFrame(average_range)
    st.table(df_range)

    # Create input fields for user input
    st.header("Enter the required information:")
    hemoglobin = st.number_input("Hemoglobin (g/dL)", value=12.0, min_value=0.0, step=0.1)
    gender = st.selectbox("Gender", ['Male', 'Female'], index=0)
    mcv = st.number_input("MCV (fL)", value=90.0,min_value=0.0, step=0.1)


    # Check if the user has entered valid values
    if st.button("Detect"):
        if hemoglobin and gender and mcv:
            # Call the predict_anemia function to get the prediction
            prediction = predict_anemia(hemoglobin, gender, mcv)

            # Map the prediction to the corresponding label
            prediction_label = 'Anemic' if prediction == 1 else 'Non Anemic'

            # Display the prediction
            st.write("The person is likely to be", prediction_label)
        else:
            st.warning("Please enter valid values.")
      

# Run the app
if __name__ == '__main__':
    app = Flask(__name__)

    @app.route('/predict', methods=['POST'])
    def predict():
        try:
        # Get request data as JSON
            data = request.get_json()

        # Extract input values
            hemoglobin = data.get('hemoglobin')
            gender = data.get('gender')
            mcv = data.get('mcv')

        # Call the predict_anemia function to get the prediction
            prediction = predict_anemia(hemoglobin, gender, mcv)

        # Map the prediction to the corresponding label
            prediction_label = 'Anemic' if prediction == 1 else 'Non Anemic'

        # Return prediction as JSON response
            return jsonify({"prediction": prediction_label})

        except Exception as e:
        # Return error message if an exception occurs
            return jsonify({"error": str(e)})

    @app.route('/')
    def home():
        return render_template('index.html')

    app.run(debug=True,host='0.0.0.0',port=8080)

