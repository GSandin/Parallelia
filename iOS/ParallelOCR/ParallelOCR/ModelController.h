//
//  ModelController.h
//  ParallelOCR
//
//  Created by Gustavo Sandín Carral on 5/3/15.
//  Copyright (c) 2015 Gustavo Sandín Carral. All rights reserved.
//

#import <UIKit/UIKit.h>

@class DataViewController;

@interface ModelController : NSObject <UIPageViewControllerDataSource>

- (DataViewController *)viewControllerAtIndex:(NSUInteger)index storyboard:(UIStoryboard *)storyboard;
- (NSUInteger)indexOfViewController:(DataViewController *)viewController;

@end

