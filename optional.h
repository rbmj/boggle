#ifndef OPTIONAL_H_INC
#define OPTIONAL_H_INC

#include <type_traits>
#include <utility>

template <class T, class U>
struct is_assignable {
private:
    typedef char true_t;
    typedef int false_t;
    template <class T1, class U1>
    static auto test(int) -> decltype(
        (std::declval<T1>() = std::declval<U1>()), std::declval<true_t>());
    static false_t test(...);
public:
    static constexpr bool value = sizeof(test(0)) == sizeof(true_t);
};

template <class T>
class optional {
    friend class optional;
    template <class U>
    struct has_ctor_and_assign {
        constexpr static bool value = std::is_constructible<T, U>::value
            && is_assignable<T, U>::value;
    };
    template <class U>
    struct collapse_optional {
        typedef optional<U> type;
    };
    template <class U>
    struct collapse_optional<optional<U>> {
        typedef collapse_optional<U> type;
    };

    bool valid;

public:
    optional() : valid(false) {}
    template <class U = T, class = typename
        std::enable_if<has_ctor_and_assign<const U&>::value>::type>
    optional(const optional<U>& other) {
        if (other.valid) {
            emplace(*(other.data()));
        }
    }
    template <class U = T, class = typename
        std::enable_if<has_ctor_and_assign<U&&>::value>::type>
    optional(optional<U>&& other) {
        if (other.valid) {
            emplace(std::move(*(other.data())));
        }
    }
    template <class... Args>
    optional(Args&&... args) {
        emplace(std::forward<Args>(args)...);
    }
    template <class... Args>
    void emplace(Args&&... args) {
        if (valid) {
            data()->~T();
            valid = false;
        }
        new (data_void()) T(std::forward<Args>(args)...);
        valid = true;
    }
    T* operator->() {
        return valid ? data() : nullptr;
    }
    const T* operator->() const {
        return valid ? data() : nullptr;
    }
    T& operator*() {
        return *(this->operator->());
    }
    const T& operator*() const {
        return *(this->operator->());
    }
    const T& get_or(const T& default_value) const {
        return valid ? *(data()) : default_value;
    }
    operator bool() const {
        return valid;
    }
    bool operator!() const {
        return !valid;
    }
    template <class U = T, class = typename
        std::enable_if<has_ctor_and_assign<const U&>::value>::type>
    optional<U>& operator=(const optional<U>& other) {
        if (other.valid) {
            if (valid) {
                *(data()) = *(other.data());
            }
            else {
                emplace(*(other.data()));
            }
        }
        else {
            destroy();
        }
    }
    template <class U = T, class = typename
        std::enable_if<has_ctor_and_assign<U&&>::value>::type>
    optional<U>& operator=(optional<U>&& other) {
        if (other.valid) {
            if (valid) {
                *(data()) = std::move(*(other.data()));
            }
            else {
                emplace(std::move(*(other.data())));
            }
        }
        else {
            destroy();
        }
    }
    //bind, haskell style
    template <class Func>
    auto operator[](Func func) -> typename
        collapse_optional<decltype(func(std::declval<T>()))>::type
    {
        if (valid) {
            return {func(*(data()))};
        }
        else {
            return {};
        }
    }
private:
    typename std::aligned_storage<sizeof(T), alignof(T)>::type m_data;
    T* data() {
        return static_cast<T*>(data_void());
    }
    const T* data() const {
        return static_cast<const T*>(data_void());
    }
    void* data_void() {
        return static_cast<void*>(&m_data);
    }
    const void* data_void() const {
        return static_cast<const void*>(&m_data);
    }
    void destroy() {
        data()->~T();
        valid = false;
    }
};

#endif
