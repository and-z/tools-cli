(ns it.zimpel.tools.cli.component-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [it.zimpel.tools.cli.component :as sut]))

(defn contains-errors? [s]
  (some? (re-find #"(?i)error" s)))

(def cli-spec [["-d" "--db-uri URI"     "(required) Datomic Database URI"]
               ["-l" "--api-url URL"    "(required) Endpoint URL"]
               ["-p" "--port PORT"      "(required) HTTP Port number" :parse-fn #(Integer/parseInt %)]
               ["-s" "--page-size SIZE" "Number of elements in a Response page" :parse-fn #(Integer/parseInt %)]
               ["-h" "--help"           "Print this help"]])

(deftest test-start
  (testing "test public API"

    (let [required #{:db-uri}
          result (atom nil)
          capture-ok #(reset! result {:ok %})
          capture-error #(reset! result {:notok %})]
      (sut/start '("-h") {:cli-spec cli-spec,
                          :required required,
                          :print-summary? false
                          :error-fn capture-error
                          :ok-fn capture-ok})
      (is (true? (-> @result :ok :show-help?)) "Expect the ok fn to capture the help result")
      (is (not (contains-errors? (-> @result :ok :summary)))))

    (let [required #{:db-uri :port}
          result (atom nil)
          capture-ok #(reset! result {:ok %})
          capture-error #(reset! result {:notok %})]
      (sut/start '("--port" "8080" "-l" "some-string") {:cli-spec cli-spec,
                                                        :required required,
                                                        :print-summary? false
                                                        :error-fn capture-error
                                                        :ok-fn capture-ok})
      (is (nil? (-> @result :ok)))
      (is (= #{:db-uri} (-> @result :notok :missing-args)))
      (is (= :missing-required-args  (-> @result :notok :error))
            "Expect the error fn to capture the error"))

    (let [required #{:db-uri}
          result (atom nil)
          capture-result #(reset! result %)]
      (sut/start '("-d" "datomic-uri" "--port" "8080") {:cli-spec cli-spec,
                                                        :required required,
                                                        :print-summary? false
                                                        :error-fn capture-result
                                                        :ok-fn capture-result})
      (is (= {:db-uri "datomic-uri" :port 8080} @result)
            "Expect the ok-fn to be called with parsed options"))))
