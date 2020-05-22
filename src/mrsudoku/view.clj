
(ns mrsudoku.view
  (:require
   [mrsudoku.grid :as g]
   [seesaw.core :refer [frame label text config! grid-panel
                        horizontal-panel vertical-panel button separator pack! show! invoke-later]]
   [seesaw.border :refer [line-border]]
   [mrsudoku.solver :as s]
   [mrsudoku.saveorchargeGrid :as sc]))

(def default-color "white")
(def conflict-color "red")
(def set-color "blue")
(def solved-color "green")

(declare show-updated-grid)

(defn mk-cell-view
  [cell cx cy ctrl]
  (case (:status cell)
    :init (label :text (str (:value cell))
                 :h-text-position :center
                 :v-text-position :center
                 :halign :center
                 :valign :center
                 :background default-color)
    :empty (let [cell-widget (text :columns 1
                                   :halign :center
                                   :id (keyword (str "cell-" cx "-" cy))
                                   :foreground set-color
                                   :background default-color)]
             (config! cell-widget
                      :listen [:document
                               ;; XXX: normally, we should not depend from the controller
                               ;;      but it's an emblamatic counter-example
                               ((resolve 'mrsudoku.control/cell-input-handler) ctrl cell-widget cx cy)])
             cell-widget)
;; A new :status is possible for the solved cells which will be diplayed over a green background
    :solved (label :text (str (:value cell))
                   :h-text-position :center
                   :v-text-position :center
                   :halign :center
                   :valign :center
                   :background solved-color)
    (throw (ex-info "Can only build widget for :solved, :init or :empty cells." {:cell cell
                                                                                 :cx cx
                                                                                 :cy cy}))))

(defn mk-block-view
  [block bref ctrl]
  (let [cell-widgets (g/reduce-block
                      (fn [widgets _ cx cy cell]
                        (conj widgets (mk-cell-view cell cx cy ctrl))) [] block bref)]
    (grid-panel :rows 3
                :columns 3
                :hgap 3
                :vgap 3
                :border (line-border :thickness 2 :color "black")
                :items cell-widgets
                :id (keyword (str "block-" bref)))))

(defn mk-grid-view [grid ctrl]
  (let [block-widgets (for [i (range 1 10)]
                        (mk-block-view (g/block grid i) i ctrl))]
    (grid-panel :rows 3
                :columns 3
                :border 6
                :hgap 6
                :vgap 6
                :items (into [] block-widgets))))


(defn mk-updated-frame
  [grid ctrl]
  (let [g-widget (mk-grid-view grid ctrl)
        main-frame (frame :title "MrSudoku"
                          :content (horizontal-panel
                                    :items [g-widget [:fill-h 32]
                                            (vertical-panel
                                             :items [:fill-v
                                                     (grid-panel
                                                      :columns 1
                                                      :vgap 10
                                                      :items [(button :text "Solve"
                                                                      :listen [:action (fn [event] (show-updated-grid (s/bruteforce-solve grid)))])])
                                                     :fill-v])
                                            [:fill-h 32]])
                          :minimum-size [640 :by 450])]
    (swap! ctrl #(assoc % :grid-widget g-widget :main-frame main-frame))
    main-frame))

(defn show-updated-grid
  [grid]
  (invoke-later
   (-> (mk-updated-frame grid (atom {:grid grid}))
       pack!
       show!)))

(defn mk-main-frame [grid ctrl]
  (let [grid-widget (mk-grid-view grid ctrl)
        main-frame (frame :title "MrSudoku"
                          :content (horizontal-panel
                                    :items [grid-widget
                                            [:fill-h 32]
                                            (vertical-panel
                                             :items [:fill-v
                                                     (grid-panel
                                                      :columns 1
                                                      :vgap 10
                                                      :items [(button :text "New Random Grid (easy)"
                                                                      :listen [:action (fn [event] (show-updated-grid (s/mk-easy-grid)))])
                                                              (button :text "New Random Grid (intermediate)"
                                                                      :listen [:action (fn [event] (show-updated-grid (s/mk-interm-grid)))])
                                                              (button :text "New Random Grid (hard)"
                                                                      :listen [:action (fn [event] (show-updated-grid (s/mk-hard-grid)))])
                                                              (button :text "Save grid"
                                                                      :listen [:action (fn [event] (sc/save-grid grid))])
                                                              (button :text "Charge last saved grid"
                                                                      :listen [:action (fn [event] (show-updated-grid (sc/charge-last-grid)))])
                                                              (button :text "Charge a previously saved grid"
                                                                      :listen [:action (fn [event] (show-updated-grid (sc/charge-randprev-grid)))])
                                                              (button :text "Solve Naïvely"
                                                                      :listen [:action (fn [event] (show-updated-grid (s/bruteforce-solve grid)))])
                                                              (button :text "Quit"
                                                                      :listen [:action (fn [event] (System/exit 0))])])
                                                     :fill-v])
                                            [:fill-h 32]])
                          :minimum-size [670 :by 550]
                          :on-close :exit)]
    (swap! ctrl #(assoc % :grid-widget grid-widget :main-frame main-frame))
    main-frame))

(defn update-cell-view!
  [cell cell-widget]
  (case (:status cell)
    :conflict (config! cell-widget :background conflict-color)
    (:set :init :empty) (config! cell-widget :background default-color)
    :solved (config! cell-widget :background solved-color :editable? false)
    (throw (ex-info "Cannot update cell widget." {:cell cell :cell-widget cell-widget}))))



