from google import genai
from google.genai import types
import wave
import json
import re
from time import sleep

# Set up the wave file to save the output:
def wave_file(filename, pcm, channels=1, rate=24000, sample_width=2):
    with wave.open(filename, "wb") as wf:
        wf.setnchannels(channels)
        wf.setsampwidth(sample_width)
        wf.setframerate(rate)
        wf.writeframes(pcm)

client = genai.Client(api_key="AIzaSyDmYV4qtRyd4fpeMTuBBpXl9LPzXoU12Jc")

with open("phrases.json") as f:
    data = json.load(f)
    for s in data:
        # Convert to legal Linux filename: remove punctuation and spaces, add .wav extension
        filename = re.sub(r'[^\w\s-]', '', s)  # Remove punctuation except hyphens
        filename = re.sub(r'\s+', '_', filename)  # Replace spaces with underscores
        filename = filename.lower() + '.wav'  # Lowercase and add .wav extension
        print(f"Processing: '{s}' -> '{filename}'")
        response = client.models.generate_content(
           model="gemini-2.5-flash-preview-tts",
            contents=f"Say quickly and in an australian accent: {s}",
            config=types.GenerateContentConfig(
            response_modalities=["AUDIO"],
            speech_config=types.SpeechConfig(
                voice_config=types.VoiceConfig(
                    prebuilt_voice_config=types.PrebuiltVoiceConfig(
                    voice_name='Zephyr',
                ))),
            ),
        )
        data = response.candidates[0].content.parts[0].inline_data.data
        wave_file(filename, data) # Saves the file to current directory
        print(f"Saved audio to {filename}")
        sleep(30)


