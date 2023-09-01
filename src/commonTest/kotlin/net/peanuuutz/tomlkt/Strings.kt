package net.peanuuutz.tomlkt

internal const val cargo: String = """
    [package]
    name = "gtk-rs-examples"
    version = "0.0.1"
    authors = ["The Gtk-rs Project Developers"]
    autobins = false
    
    [dependencies]
    chrono = "0.4"
    futures = "0.3"
    atk = "^0"
    glib-sys = "^0"
    gobject-sys = "^0"
    glib = "^0"
    gio = "^0"
    gdk = "^0"
    gdk-pixbuf = "^0"
    gtk = "^0"
    once_cell = "^0"
    pango = "^0"
    pangocairo = "^0"
    cairo-rs = { version = "^0", features = ["png"] }
    
    [dependencies.async-tls]
    version = "0.6"
    optional = true
    
    [features]
    #default = ["gtk_3_22_30"]
    gtk_3_18 = ["gtk/v3_18", "gdk-pixbuf/v2_32", "gdk/v3_18", "gio/v2_46", "glib/v2_46", "pango/v1_38"] #for CI tools
    gtk_3_22_30 = ["gtk_3_18", "gtk/v3_22_30", "gdk-pixbuf/v2_36", "gdk/v3_22", "gio/v2_56", "glib/v2_56", "pango/v1_42"] #for CI tools
    gtk_3_24 = ["gtk_3_22_30", "gtk/v3_24", "atk/v2_30", "gdk-pixbuf/v2_36_8", "gdk/v3_24", "gio/v2_58", "glib/v2_58"] #for CI tools
    
    [[bin]]
    name = "accessibility"
    
    [[bin]]
    name = "basic"
    
    [[bin]]
    name = "basic_subclass"
    
    [[bin]]
    name = "builder_basics"
    
    [[bin]]
    name = "builder_signal"
    
    [[bin]]
    name = "builders"
    
    [[bin]]
    name = "cairo_png"
    
    [[bin]]
    name = "cairo_threads"
    
    [[bin]]
    name = "cairotest"
    
    [[bin]]
    name = "child-properties"
    
    [[bin]]
    name = "clipboard_simple"
    
    [[bin]]
    name = "clock"
    
    [[bin]]
    name = "clone_macro"
    
    [[bin]]
    name = "communication_thread"
    edition = "2018"
    
    [[bin]]
    name = "css"
    
    [[bin]]
    name = "drag_and_drop"
    
    [[bin]]
    name = "drag_and_drop_textview"
    
    [[bin]]
    name = "gio_futures"
    
    [[bin]]
    name = "gio_futures_await"
    edition = "2018"
    
    [[bin]]
    name = "grid"
    
    [[bin]]
    name = "gtktest"
    
    [[bin]]
    name = "iconview_example"
    
    [[bin]]
    name = "listbox_model"
    required-features = ["gtk/v3_16", "gio/v2_44"]
    
    [[bin]]
    name = "menu_bar"
    
    [[bin]]
    name = "menu_bar_system"
    
    [[bin]]
    name = "multi_windows"
    
    [[bin]]
    name = "multithreading_context"
    
    [[bin]]
    name = "notebook"
    
    [[bin]]
    name = "overlay"
    
    [[bin]]
    name = "pango_attributes"
    
    [[bin]]
    name = "progress_tracker"
    path = "src/bin/progress_tracker.rs"
    
    [[bin]]
    name = "simple_treeview"
    
    [[bin]]
    name = "sync_widgets"
    
    [[bin]]
    name = "text_viewer"
    
    [[bin]]
    name = "transparent_main_window"
    
    [[bin]]
    name = "tree_model_sort"
    
    [[bin]]
    name = "treeview"
    
    [[bin]]
    name = "list_store"
    
    [[bin]]
    name = "entry_completion"
    
    [[bin]]
    name = "printing"
    
    [[bin]]
    name = "gio_async_tls"
    required-features = ["async-tls"]
    edition = "2018"
"""

internal const val project: String = """
    name = "tomlkt"
    maintainability = "HIGH"
    description = '''
    This is my first project, so sorry for any inconvenience! \
    Anyway, constructive criticism is welcomed. :)'''
    
    [owner]
    name = "Peanuuutz"
    account = { username = "peanuuutz", password = "123456" }
"""

internal const val score: String = """
    examinee = "Peanuuutz"
    
    [scores]
    Listening = 91
    Writing = 83
"""

internal const val lyrics: String = """
    Do, a deer, a female deer.
    Re, a drop of golden sun.
"""

internal const val anotherLyrics: String = """
    Oops my baby,
    you woke up in my bed.
"""

internal const val thirdLyrics: String = "Oops we broke up,\nwe're better off as friends."

internal const val boxContent: String = "content = null"

internal const val integers: String = """
    two = 0b100
    eight = 0o100
    ten = -100
    sixteen = 0x100
"""

internal const val externalModule: String = """
    name = "example"
    id = 4321234
"""

internal const val dateTimes: String = """
    local-date-time = 2020-01-01T20:00:00.5
    offset-date-time = 1999-09-09T09:09:09.999999-09:00
    local-date = 2020-01-01
    local-time = 09:09:09.999999
"""

internal const val randomTask: String = """
    name = "job"
    date = 2000-01-01T12:00:00
"""
