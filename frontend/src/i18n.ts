// src/i18n.ts
import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import LanguageDetector from "i18next-browser-languagedetector";

const resources = {
  en: {
    translation: {
      nav: {
        orders: "Orders",
        addresses: "Addresses",
        profile: "Profile",
        login: "Login",
        register: "Register",
        logout: "Logout",
        themeDark: "Dark",
        themeLight: "Light",
        language: "Language",
      },

      common: {
        refresh: "Refresh",
        loading: "Loading...",
        save: "Save",
        cancel: "Cancel",
        delete: "Delete",
        open: "Open",
        back: "Back",
        create: "Create",
        clear: "Clear",

        edit: "Edit",          // ✅ добавлено
        saving: "Saving...",
        deleting: "Deleting...",
      },

      auth: {
        login: "Login",
        subtitleLogin: "Sign in to manage addresses and orders",
        register: "Register",
        subtitleRegister: "Create a new account",

        email: "Email",
        password: "Password",

        signIn: "Sign in",
        signingIn: "Signing in...",
        createAccount: "Create account",
        creating: "Creating...",

        noAccount: "No account?",
        haveAccount: "Already have an account?",

        tip: "Tip: use your test user from backend (e.g.: {{email}}).",

        passwordHint: "min 4 chars",
        passwordValidationNote:
          "Password validation is minimal on frontend. Backend will validate anyway.",
      },

      addresses: {
        title: "Addresses",
        subtitle: "Manage delivery addresses",

        add: "Add address",
        edit: "Edit address", // ✅ добавлено (для режима редактирования)

        saved: "{{count}} saved",
        clear: "Clear",

        firstName: "First name",
        lastName: "Last name",
        patronymic: "Patronymic",
        phone: "Phone",
        city: "City",
        street: "Street",
        building: "Building",
        postalCode: "Postal code",

        yourAddresses: "Your addresses",
        noAddresses: "No addresses yet.", // ✅ добавлено (использует страница)

        // оставил совместимость со старыми ключами (если где-то ещё используются)
        your: "Your addresses",
        none: "No addresses yet.",
      },

      orders: {
        title: "Orders",
        subtitle: "Create, edit and view your orders",
        createOrder: "Create order",
        name: "Name",
        address: "Address",
        recent: "Recent",
        noOrders: "No orders yet.",
        itemsCount_one: "{{count}} item",
        itemsCount_other: "{{count}} items",
        id: "id",
        created: "Created",
        edit: "Edit",
        delete: "Delete",

        orderNamePlaceholder: "My first order",
        orderNameEditPlaceholder: "Order name",
      },

      order: {
        addItem: "Add item",
        hint: "link + size + configuration",
        items: "Items",
        noItems: "No items yet.",
        size: "size",
        configuration: "configuration",
        price: "price",
        created: "Created",
      },

      item: {
        title: "Item {{id}}",
        editTitle: "Edit item",
        orderId: "orderId",
      },

      profile: {
        title: "Profile",
        subtitle: "Your account info",
        email: "Email",
        userId: "User ID",
        copyEmail: "Copy email",
        copyId: "Copy id",
        rawJson: "Raw JSON",
      },

      errors: {
        fillAll: "Fill link / size / configuration",
        fillOrderAndAddress: "Fill order name and select address",

        loadOrderFail: "Failed to load order",
        addItemFail: "Failed to add item",
        deleteFail: "Delete failed",
        loadItemFail: "Failed to load item",
        updateItemFail: "Failed to update item",

        noItemIdOpen: "Item has no id — cannot open",
        noItemIdDelete: "Item has no id — cannot delete",

        createAddressFail: "Create address failed",
        loadAddressesFail: "Failed to load addresses",
        loginFail: "Login failed",
        registerFail: "Register failed",
        createOrderFail: "Create order failed",
        updateOrderFail: "Update order failed",
        deleteOrderFail: "Delete order failed",
      },
    },
  },

  ru: {
    translation: {
      nav: {
        orders: "Заказы",
        addresses: "Адреса",
        profile: "Профиль",
        login: "Вход",
        register: "Регистрация",
        logout: "Выйти",
        themeDark: "Тёмная",
        themeLight: "Светлая",
        language: "Язык",
      },

      common: {
        refresh: "Обновить",
        loading: "Загрузка...",
        save: "Сохранить",
        cancel: "Отмена",
        delete: "Удалить",
        open: "Открыть",
        back: "Назад",
        create: "Создать",
        clear: "Очистить",

        edit: "Изменить",      // ✅ добавлено
        saving: "Сохраняем...",
        deleting: "Удаляем...",
      },

      auth: {
        login: "Вход",
        subtitleLogin: "Войдите, чтобы управлять адресами и заказами",
        register: "Регистрация",
        subtitleRegister: "Создайте новый аккаунт",

        email: "Email",
        password: "Пароль",

        signIn: "Войти",
        signingIn: "Входим...",
        createAccount: "Создать аккаунт",
        creating: "Создаём...",

        noAccount: "Нет аккаунта?",
        haveAccount: "Уже есть аккаунт?",

        tip: "Подсказка: используй тестового пользователя из бэка (например: {{email}}).",

        passwordHint: "мин 4 символа",
        passwordValidationNote:
          "Проверка пароля на фронте минимальная. Бэк всё равно проверит.",
      },

      addresses: {
        title: "Адреса",
        subtitle: "Управление адресами доставки",

        add: "Добавить адрес",
        edit: "Редактирование адреса", // ✅ добавлено

        saved: "Сохранено: {{count}}",
        clear: "Очистить",

        firstName: "Имя",
        lastName: "Фамилия",
        patronymic: "Отчество",
        phone: "Телефон",
        city: "Город",
        street: "Улица",
        building: "Дом/кв",
        postalCode: "Индекс",

        yourAddresses: "Ваши адреса",
        noAddresses: "Адресов пока нет.", // ✅ добавлено

        // совместимость со старыми ключами
        your: "Ваши адреса",
        none: "Адресов пока нет.",
      },

      orders: {
        title: "Заказы",
        subtitle: "Создавай, редактируй и смотри свои заказы",
        createOrder: "Создать заказ",
        name: "Название",
        address: "Адрес",
        recent: "Последние",
        noOrders: "Пока нет заказов.",
        itemsCount_one: "{{count}} шт.",
        itemsCount_few: "{{count}} шт.",
        itemsCount_many: "{{count}} шт.",
        itemsCount_other: "{{count}} шт.",
        id: "id",
        created: "Создан",
        edit: "Изменить",
        delete: "Удалить",

        orderNamePlaceholder: "Мой первый заказ",
        orderNameEditPlaceholder: "Название заказа",
      },

      order: {
        addItem: "Добавить товар",
        hint: "ссылка + размер + конфигурация",
        items: "Товары",
        noItems: "Товаров пока нет.",
        size: "размер",
        configuration: "конфигурация",
        price: "цена",
        created: "Создан",
      },

      item: {
        title: "Товар {{id}}",
        editTitle: "Редактирование товара",
        orderId: "orderId",
      },

      profile: {
        title: "Профиль",
        subtitle: "Информация об аккаунте",
        email: "Email",
        userId: "User ID",
        copyEmail: "Скопировать email",
        copyId: "Скопировать id",
        rawJson: "Raw JSON",
      },

      errors: {
        fillAll: "Заполни link / size / configuration",
        fillOrderAndAddress: "Заполни имя заказа и выбери адрес",

        loadOrderFail: "Не удалось загрузить заказ",
        addItemFail: "Не удалось добавить товар",
        deleteFail: "Не удалось удалить",
        loadItemFail: "Не удалось загрузить товар",
        updateItemFail: "Не удалось обновить товар",

        noItemIdOpen: "У товара нет id — нельзя открыть",
        noItemIdDelete: "У товара нет id — нельзя удалить",

        createAddressFail: "Не удалось создать адрес",
        loadAddressesFail: "Не удалось загрузить адреса",
        loginFail: "Не удалось войти",
        registerFail: "Регистрация не удалась",
        createOrderFail: "Не удалось создать заказ",
        updateOrderFail: "Не удалось обновить заказ",
        deleteOrderFail: "Не удалось удалить заказ",
      },
    },
  },
} as const;

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources,
    fallbackLng: "ru",
    supportedLngs: ["ru", "en"],
    interpolation: { escapeValue: false },
    detection: {
      order: ["localStorage", "navigator"],
      caches: ["localStorage"],
      lookupLocalStorage: "lang",
    },
  });

export default i18n;
