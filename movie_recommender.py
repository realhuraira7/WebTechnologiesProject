import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from difflib import get_close_matches
import numpy as np
import ipywidgets as widgets
from IPython.display import display, clear_output, HTML

# --- Initial Movie Dataset ---
movies = {
    "title": [
        "Inception", "Interstellar", "The Prestige", "Avatar", "Titanic",
        "Avengers Endgame", "The Dark Knight", "Joker", "Spider Man", "Iron Man",
        "The Matrix", "Gladiator", "The Lion King", "Toy Story", "Finding Nemo"
    ],
    "genre": [
        "Science Fiction Action Thriller",
        "Science Fiction Space Drama",
        "Mystery Thriller Drama",
        "Fantasy Adventure Science Fiction",
        "Romance Drama",
        "Action Adventure Science Fiction",
        "Action Crime Drama Thriller",
        "Crime Drama Thriller",
        "Action Adventure Fantasy",
        "Action Science Fiction",
        "Science Fiction Action",
        "Action History Drama",
        "Animation Adventure Family",
        "Animation Comedy Family",
        "Animation Adventure Family"
    ]
}
df = pd.DataFrame(movies)

tfidf = None
similarity = None

def rebuild_model():
    global tfidf, similarity
    tfidf = TfidfVectorizer()
    vectors = tfidf.fit_transform(df["genre"])
    similarity = cosine_similarity(vectors)

rebuild_model()

def match_title(query):
    titles_lower = df["title"].str.lower().tolist()
    q = query.lower().strip()
    if q in titles_lower:
        return df["title"][titles_lower.index(q)]
    close = get_close_matches(q, titles_lower, n=1, cutoff=0.5)
    if close:
        return df["title"][titles_lower.index(close[0])]
    return None

def get_genres(title):
    return set(df[df["title"] == title]["genre"].values[0].split())

def get_recommendations(selected_titles, top_n=5):
    indices = [df[df["title"] == t].index[0] for t in selected_titles]
    blended_scores = np.mean(similarity[indices], axis=0)
    results = []
    for i, score in enumerate(blended_scores):
        if i not in indices:
            results.append((df.iloc[i]["title"], score))
    results.sort(key=lambda x: x[1], reverse=True)
    return results[:top_n]

# ================= UI =================

multi_select = widgets.SelectMultiple(
    options=df["title"].tolist(),
    layout=widgets.Layout(height="180px", width="320px")
)
search_box = widgets.Text(
    placeholder="...or type movie name(s), comma-separated",
    layout=widgets.Layout(width="320px")
)
button = widgets.Button(
    description="Get Recommendations",
    layout=widgets.Layout(width="320px", height="42px"),
    style={"button_color": "#8E2DE2", "font_weight": "bold"}
)
button.style.text_color = "white"
output = widgets.Output()

new_title_box = widgets.Text(placeholder="New movie title", layout=widgets.Layout(width="320px"))
new_genre_box = widgets.Text(placeholder="Genres, space-separated (e.g. Action Comedy)", layout=widgets.Layout(width="320px"))
add_button = widgets.Button(
    description="Add Movie",
    layout=widgets.Layout(width="320px", height="38px"),
    style={"button_color": "#00CEC9", "font_weight": "bold"}
)
add_button.style.text_color = "white"
add_status = widgets.Output()

def on_add_click(b):
    global df
    with add_status:
        clear_output()
        title = new_title_box.value.strip()
        genre = new_genre_box.value.strip()
        if not title or not genre:
            display(HTML("<p style='color:#ff7675;'>Enter both a title and at least one genre.</p>"))
            return
        if title.lower() in df["title"].str.lower().values:
            display(HTML("<p style='color:#ff7675;'>That movie already exists.</p>"))
            return
        df = pd.concat([df, pd.DataFrame([{"title": title, "genre": genre}])], ignore_index=True)
        rebuild_model()
        multi_select.options = df["title"].tolist()
        new_title_box.value = ""
        new_genre_box.value = ""
        display(HTML("<p style='color:#00ff88;'>Added! It now appears in the list.</p>"))

add_button.on_click(on_add_click)

COLORS = ["#8E2DE2", "#00CEC9", "#0984E3", "#FD79A8", "#FDCB6E", "#00B894"]

def render_genre_tags(movie_genres, shared_genres):
    tags = ""
    for g in sorted(movie_genres):
        if g in shared_genres:
            tags += (
                "<span style='background:#00ff8833;color:#00ff88;border:1px solid #00ff8877;"
                "padding:3px 9px;border-radius:10px;font-size:11px;display:inline-block;"
                "font-weight:600;'>&#10003; " + g + "</span>"
            )
        else:
            tags += (
                "<span style='background:#ffffff11;color:#b2bec3;border:1px solid #ffffff22;"
                "padding:3px 9px;border-radius:10px;font-size:11px;display:inline-block;'>" + g + "</span>"
            )
    return "<div style='display:flex;flex-wrap:wrap;gap:6px;'>" + tags + "</div>"

def render_card(title, score, color, shared_genres):
    pct = round(score * 100)
    movie_genres = get_genres(title)
    tags_html = render_genre_tags(movie_genres, shared_genres)
    return (
        "<div style='background:#1e1e2f;border:1px solid " + color + "55;"
        "box-shadow:0 0 12px " + color + "33;border-radius:14px;padding:14px 18px;"
        "margin:10px 0;font-family:sans-serif;'>"
        "<div style='display:flex;justify-content:space-between;align-items:center;'>"
        "<span style='font-size:17px;font-weight:700;color:#ffffff;'>" + title + "</span>"
        "<span style='font-size:14px;font-weight:bold;color:" + color + ";"
        "background:" + color + "22;padding:3px 10px;border-radius:12px;'>" + str(pct) + "% match</span>"
        "</div>"
        "<div style='background:#2d2d44;border-radius:8px;height:10px;margin-top:10px;overflow:hidden;'>"
        "<div style='background:linear-gradient(90deg," + color + ",#ffffff55);"
        "width:" + str(pct) + "%;height:100%;border-radius:8px;"
        "box-shadow:0 0 8px " + color + ";'></div>"
        "</div>"
        "<div style='margin-top:10px;'>" + tags_html + "</div>"
        "</div>"
    )

def on_click(b):
    with output:
        clear_output()
        selected = list(multi_select.value)

        if search_box.value.strip():
            typed = [t.strip() for t in search_box.value.split(",")]
            for t in typed:
                matched = match_title(t)
                if matched and matched not in selected:
                    selected.append(matched)
                elif not matched:
                    display(HTML("<p style='color:#ff7675;'>Could not match: " + t + "</p>"))

        if not selected:
            display(HTML("<p style='color:#ff7675;'>Pick or type at least one movie first.</p>"))
            return

        your_genres = set()
        for t in selected:
            your_genres |= get_genres(t)

        chips = "".join(
            "<span style='background:#8E2DE2;color:white;padding:5px 12px;"
            "border-radius:20px;font-size:13px;margin-right:6px;display:inline-block;"
            "margin-bottom:6px;'>" + t + "</span>"
            for t in selected
        )
        your_tags = render_genre_tags(your_genres, your_genres)
        display(HTML(
            "<div style='margin-bottom:6px;'>" + chips + "</div>"
            "<div style='margin-bottom:14px;'>"
            "<span style='color:#636e72;font-size:12px;'>Your genres:</span><br>" + your_tags +
            "</div>"
        ))

        results = get_recommendations(selected)
        cards_html = "".join(
            render_card(title, score, COLORS[i % len(COLORS)], your_genres)
            for i, (title, score) in enumerate(results)
        )
        display(HTML(cards_html))

button.on_click(on_click)

display(HTML(
    "<div style='background:linear-gradient(135deg,#8E2DE2,#4A00E0);"
    "padding:20px 24px;border-radius:16px;margin-bottom:14px;'>"
    "<h1 style='color:white;margin:0;font-size:26px;'>Movie Recommender</h1>"
    "<p style='color:#eeeeee;margin-top:6px;font-size:14px;'>Pick favorites, add your own, get instant matches</p>"
    "</div>"
))
display(HTML("<h4 style='color:#6C5CE7;'>Choose from the list</h4>"))
display(multi_select, search_box, button, output)
display(HTML("<h4 style='color:#00CEC9;margin-top:20px;'>Or add a new movie</h4>"))
display(new_title_box, new_genre_box, add_button, add_status)
