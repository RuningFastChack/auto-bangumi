declare namespace Menu {
  interface MenuOptions {
    path?: string;
    name?: string;
    component?: string | (() => Promise<unknown>);
    redirect?: string;
    meta: MetaProps;
    children?: MenuOptions[];
  }

  interface MetaProps {
    icon?: string;
    title?: string;
    mainTitle?: string;
    mainMenu?: boolean;
    viewConfig?: ViewConfig;
    breadcrumbs?: Array<{
      name: string;
      path: string;
      mainMenu?: boolean;
    }>;
  }

  interface ViewConfig {
    width?: number;
    height?: number | string;
    margin?: string;
  }
}
