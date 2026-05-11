export class ResourceTable& {
    pkgName@entry;

    static func_main_0(param_0, param_1, param_2): void     {
        return;
    }
}
export class EntryAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        try {
        } catch (e) {
            throw v2;
        }
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        if (true) {
            const info = v6.info;
            const v8 = "testTag";
            const v9 = "Succeeded in loading the content.";
            v6.info(v7, v8, v9);
            return;
        }
        const v8 = "testTag";
        const v9 = "Failed to load the content. Cause: %{public}s";
        const v11 = JSON;
        v11.stringify(v16);
        stringify = v11.stringify(v12);
        v6.error(v7, v8, v9);
        return;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        v0 = v8;
        lex_0_0 = 0;
        let v0: EntryBackupAbility& = EntryBackupAbility&;
        export_0 = v4;
        return;
    }

    static onCreate(param_0, param_1, param_2, param_3, param_4): void     {
        try {
        } catch (e) {
            const error = v8.error;
            const v10 = "testTag";
            const v11 = "Failed to set colorMode. Cause: %{public}s";
            const v13 = JSON;
            let stringify = v13.stringify;
            const v14 = v6;
            v13.stringify(v14);
            stringify = v13.stringify(v14);
            v8.error(v9, v10, v11);
        }
        const v9 = "testTag";
        const v10 = "%{public}s";
        const v11 = "Ability onCreate";
        error.info(v8, v9, v10);
        return;
    }

    static onDestroy(param_0, param_1, param_2): void     {
        const v7 = "testTag";
        const v8 = "%{public}s";
        v5.info(v6, v7, v8);
        return;
    }

    static onBackground(param_0, param_1, param_2): void     {
        const v7 = "testTag";
        const v8 = "%{public}s";
        v5.info(v6, v7, v8);
        return;
    }

    static onForeground(param_0, param_1, param_2): void     {
        const v7 = "testTag";
        const v8 = "%{public}s";
        v5.info(v6, v7, v8);
        return;
    }

    static onWindowStageCreate(param_0, param_1, param_2, param_3): void     {
        let v8 = "testTag";
        const v9 = "%{public}s";
        v6.info(v7, v8, v9);
        const v6 = v3;
        const v7 = "pages/Index";
        v8 = onBackground;
        v6.loadContent(v7, v8);
        return;
    }

    static onWindowStageDestroy(param_0, param_1, param_2): void     {
        const v7 = "testTag";
        const v8 = "%{public}s";
        v5.info(v6, v7, v8);
        return;
    }
}
export class EntryBackupAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        try {
        } catch (e) {
            throw v2;
        }
    }

    static func_main_0(param_0, param_1, param_2): void     {
        v0 = v8;
        lex_0_0 = 0;
        let v0: EntryBackupAbility& = EntryBackupAbility&;
        export_0 = v4;
        return;
    }

    static async onBackup(param_0, param_1, param_2): void     {
        try {
            throw v5;
        } catch (e) {
            throw v3;
        }
    }

    static async onRestore(param_0, param_1, param_2, param_3): void     {
        try {
            throw v6;
        } catch (e) {
            throw v4;
        }
    }
}
export class Index& {
    pkgName@entry;

    constructor(param_0, param_1, param_2)     {
        const v4 = v2;
        v4.items = [];
        return v8;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static move(param_0, param_1, param_2, param_3, param_4): void     {
        const v6 = v3;
        if (!(v6 === "up")) {
            const v6: boolean = v6 === "up";
            if (!(v6 === "down")) {
                const v6: boolean = v6 === "down";
                const v6 = "L";
                const v7 = String;
                const v8 = v4;
                return v7(v8) + v6;
            }
            const v6 = "D";
            const v7 = String;
            const v8 = v4;
            return v7(v8) + v6;
        }
        const v6 = "U";
        const v7 = String;
        const v8 = v4;
        return v7(v8) + v6;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static add(param_0, param_1, param_2, param_3, param_4): void     {
        v4 = v14;
        let v9 = v4;
        v4 = new v4();
        v9 = v12;
        const items = v9.items;
        items.push(v4);
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        let v7 = Column;
        v7.create();
        v7 = Column;
        v7.width("100%");
        v7 = Column;
        v8 = "100%";
        v7.height(v8);
        return;
    }

    static find(param_0, param_1, param_2, param_3): void     {
        const v6 = 0;
        do {
            let v9 = v2;
            const items = v9.items;
            v9 = v6;
            const v7 = items[v9];
            v9 = v7;
            const key = v9.key;
        } while (v3 === key);
        const key = v7;
        return key.value;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        let v7 = Text;
        v7.create(lex_0_0.message);
        v7 = Text;
        message = 30;
        v7.fontSize(message);
        v7 = Text;
        message = FontWeight;
        v7.fontWeight(message.Bold);
        return;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        v0 = v22;
        v2 = v24;
        lex_0_0 = func_main_0;
        v14 = anonymous_method;
        lex_0_1 = v14;
        const v16 = ViewPU;
        const v16 = Reflect;
        let set = v16.set;
        v16.set(ViewPU.prototype, "finalizeConstruction", Index);
        prototype = undefined;
        let v2: @system.app = @system.app;
        let isUndefined: boolean = set > undefined;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = undefined;
        let v0: @system.app = @system.app;
        isUndefined = set > undefined;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = ViewPU;
        isUndefined = set > undefined;
        prototype = set.prototype;
        v19 = "message";
        let v20 = undefined;
        false[v21] = "get".v20;
        v19 = "message";
        v20 = undefined;
        v21 = testArrayFromMap;
        false[v20] = "get".v21;
        prototype2.initialRender = testIncludesFrom;
        prototype2.rerender = testTypeAssertion;
        set.getEntryName = aboutToBeDeleted;
        set = set;
        set = set;
        set = registerNamedRoute;
        isUndefined = func_48;
        prototype = "";
        prototype2 = {  };
        prototype2 = prototype2;
        return;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.get();
        return __message.get();
    }

    static testJsonOps(param_0, param_1, param_2): void     {
        {  }[v8] = 100;
        v7 = v4;
        v8 = "y";
        v7[v8] = 200;
        v8 = JSON;
        v8.stringify(v4);
        stringify = v8.stringify(v9);
        v8 = JSON;
        let parse = v8.parse;
        v9 = stringify2;
        v8.parse(v9);
        parse = v8.parse(v9);
        let v11 = String;
        let v12 = parse2;
        v12 = v12[v13];
        v11 = v11(v12);
        v9 = `,${v11}`;
        v11 = String;
        v11 = parse2;
        v12 = "y";
        v11 = v11[v12];
        v8 = v11(v11) + v9;
        parse = `,${v8}`;
        v8 = String;
        v9 = stringify2;
        const length = v9.length;
        return v8(length) + parse;
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        v5.updateDirtyElements();
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.set(v11);
        return;
    }

    static testInterface(param_0, param_1, param_2): void     {
        let v6 = {  };
        const v3 = v6;
        v6 = {  };
        const v4 = v6;
        let v8 = lex_0_0;
        v8 = v8(v3);
        v6 = `|${v8}`;
        v8 = lex_0_0;
        return v4(v4) + v6;
    }

    static getDisplayName(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
        const name = v6.name;
        const v7 = v3;
        let age = v7.age;
        if (age !== undefined) {
            let v8 = name;
            const v7: string = ` (age ${v8}`;
            v8 = String;
            const v9 = v3;
            let age = v9.age;
            age = v8(age2) + v7;
            const name: string = `)${age}`;
            const v7 = v3;
            const email = v7.email;
            if (!(email !== undefined)) {
            }
            let v8 = name;
            const v7: string = ` <${v8}`;
            v8 = v3;
            const email: number = v8.email + v7;
            const name: string = `>${email}`;
        }
        return age !== undefined;
    }

    static testPromiseAll(param_0, param_1, param_2): void     {
        let v8 = Promise;
        let v9 = anonymous_method;
        const anonymous_method: anonymous_method = new anonymous_method(v8, v9);
        v8 = Promise;
        v9 = EntryAbility;
        const entryAbility: EntryAbility = new EntryAbility(v8, v9, v10, v11, v12, v13);
        v8 = Promise;
        v9 = onWindowStageDestroy;
        const onWindowStageDestroy: onWindowStageDestroy = new onWindowStageDestroy(v8, v9, v10, v11, v12, v13, v14, v15, v16, v17);
        v9 = Promise;
        let v10: Array<unknown> = [];
        anonymous_method[10] = v11;
        entryAbility[10] = v12;
        onWindowStageDestroy[10] = v13;
        v10 = v10;
        v9.all(v10);
        all = v9.all(v10);
        return "pending";
    }

    static testConfigStore(param_0, param_1, param_2): void     {
        const acc: acc = new acc();
        let v9: acc = acc;
        v9.add("host", "localhost");
        v9 = acc;
        add = v9.add;
        v10 = "port";
        v11 = "8080";
        v9.add(v10, v11);
        v9 = acc;
        add = v9.add;
        v10 = "debug";
        v11 = "true";
        v9.add(v10, v11);
        v9 = acc;
        let find = v9.find;
        v10 = "host";
        v9.find(v10);
        find = v9.find(v10);
        v9 = acc;
        find = v9.find;
        v10 = "port";
        v9.find(v10);
        find = v9.find(v10);
        v9 = acc;
        find = v9.find;
        v10 = "unknown";
        v9.find(v10);
        find = v9.find(v10);
        v11 = find2;
        v10 = `,${v11}`;
        v9 = find3 + v10;
        find = `,${v9}`;
        return find4 + find;
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static testArrayFromMap(param_0, param_1, param_2): void     {
        let v6: Array<unknown> = [];
        const v3: Array<unknown> = [];
        v6 = [];
        const v7 = 0;
        const v8 = v7;
        const v9 = v3;
        while (!(v9.length < v8)) {
            loop_0: do {
    let v8: boolean = v9.length < v8;
    let v9 = v3;
} while (v9.length < v8);
        }
        const v7: boolean = v9.length < v8;
        const v8 = ",";
        v7.join(v8);
        return v7.join(v8);
    }

    static testIncludesFrom(param_0, param_1, param_2): void     {
        const v3: Array<unknown> = [];
        let v9: Array<unknown> = v3;
        let includes = v9.includes;
        v9.includes(3);
        includes = v9.includes(v10);
        v9 = v3;
        includes = v9.includes;
        v10 = 3;
        v9.includes(v10, 4);
        includes = v9.includes(v10, v11);
        v9 = v3;
        includes = v9.includes;
        v10 = 6;
        v9.includes(v10);
        includes = v9.includes(v10);
        let v12 = String;
        v12 = v12(includes2);
        v10 = `,${v12}`;
        v12 = String;
        v12 = includes3;
        v9 = v12(v12) + v10;
        includes = `,${v9}`;
        v9 = String;
        v10 = includes4;
        return v9(v10) + includes;
    }

    static testLiteralUnion(param_0, param_1, param_2): void     {
        let v8 = lex_0_1;
        v8 = v8("up", 3);
        let v6: string = `,${v8}`;
        v8 = lex_0_1;
        v8 = "right";
        v9 = 5;
        let v5: number = v8(v8, v9) + v6;
        const v4: string = `,${v5}`;
        v5 = lex_0_1;
        v6 = "down";
        v8 = 1;
        return v5(v6, v8) + v4;
    }

    static initialRender(param_0, param_1, param_2): void     {
        lex_0_0 = v10;
        let v5 = lex_0_0;
        v5.observeComponentCreation2(func_main_0, Column);
        v5 = lex_0_0;
        observeComponentCreation2 = v5.observeComponentCreation2;
        v6 = onWindowStageCreate;
        v7 = Text;
        v5.observeComponentCreation2(v6, v7);
        v5 = Text;
        v5.pop();
        v5 = Column;
        pop = v5.pop;
        v5.pop();
        return;
    }

    static testTypeAssertion(param_0, param_1, param_2): void     {
        let v7 = 42;
        const v3: number = v7;
        let v9: number = v3;
        const v5: number = 2 * v9;
        v7 = "hello";
        v9 = v7;
        const length = v9.length;
        let v11 = String;
        v11 = v11(v5);
        v9 = `,${v11}`;
        v11 = String;
        v11 = length;
        return v11(v11) + v9;
    }

    static testMapFromEntries(param_0, param_1, param_2): void     {
        let v9: Array<unknown> = [];
        let v10: string[] = ["/* element_0 */"];
        ["/* element_0 */"][9] = v17;
        v10 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */"][9] = v18;
        v10 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */"][9] = v19;
        const v3: Array<unknown> = v9;
        v9 = Map;
        const map = new Map(v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19);
        v10 = 0;
        let v11 = v10;
        const v12 = v3;
        while (!(v12.length < v11)) {
            loop_0: do {
    let v11: boolean = v12.length < v11;
    let v12 = v3;
} while (v12.length < v11);
        }
        let v10: boolean = v12.length < v11;
        let v11 = "a";
        v10.has(v11);
        has = v10.has(v11);
        v10 = map;
        let get = v10.get;
        v11 = "b";
        v10.get(v11);
        get = v10.get(v11);
        get = map;
        const size = get.size;
        let v13 = String;
        const v14 = has2;
        v13 = v13(v14);
        v11 = `,${v13}`;
        v13 = String;
        v13 = get2;
        v10 = v13(v13) + v11;
        get = `,${v10}`;
        v10 = String;
        v11 = size;
        return v10(v11) + get;
    }

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static aboutToBeDeleted(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.aboutToBeDeleted();
        let v7 = SubscriberManager;
        v7.Get();
        get = v7.Get();
        let id__ = v7.id__;
        v10.id__();
        id__ = v10.id__();
        get2.delete(id__);
        const get2 = v2;
        get2.aboutToBeDeletedInternal();
        return;
    }

    static setInitiallyProvidedValue(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
        const message = v6.message;
        const message = v2;
        const v6 = v3;
        message.message = v6.message;
        return;
    }

    static purgeVariableDependenciesOnElmtId(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.purgeDependencyOnElmtId(v11);
        return;
    }
}
export class @ohos.app {
    @native.ohos.app;
}
export class @ohos.curves {
    @native.ohos.curves;
}
export class @ohos.matrix4 {
    @native.ohos.matrix4;
}
export class @system.app {
    @native.system.app;
}
export class @system.curves {
    @native.system.curves;
}
export class @system.matrix4 {
    @native.system.matrix4;
}
export class @system.router {
    @native.system.router;
}
export class _ESConcurrentModuleRequestsAnnotation {
}
export class _ESExpectedPropertyCountAnnotation {
}
export class _ESSlotNumberAnnotation {
}